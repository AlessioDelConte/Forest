
/* Universita' Ca' Foscari Venezia
 * Corso di Simulazione e performance delle reti
 * Andrea Marin, Filippo Maganza, Alessio Del Conte
 * Simplified simulation of M/M/1 queue
 * 2017/2018
 */
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define Q_LIMIT 100 /*queue capacity*/
#define NUM_MAX_EVENTS 2000000
#define BUSY 1
#define IDLE 0
#define PACKET_BIG 1024
#define PACKET_SMALL 64
#define RATE_SMALL 0.2
#define NUMEVENT 2

/*Parameters*/
#define LAMBDA 0.0011
#define MU 3.0

typedef struct pacchetto {
  double RPT; // Remaining process time
  double arrival_time;
  int type; // Tipologia del pacchetto (PACKET_BIG,PACKET_SMALL)
} * Pacchetto;


double sim_time;                  /*simulated time*/
int server_status;                /*Is the server busy or idle?*/
int num_in_q;                     /*number of jobs in queue*/
double time_next_event[NUMEVENT]; /*event calendar. position 0 arrivals, position 1 departures*/
int next_event_type;              /*type of next event, 0 arrival, 1 departure*/
Pacchetto queue[Q_LIMIT];

/*for the statistics*/
double idle_time; /* per l'utilizzo */
double busy_time; /* per l'utilizzo */
double time_last_event; /*epoch of last processed event*/
double area_num_in_q;   /*to compute the expected number of customers*/
int n_arrival;
double total_waiting_time = 0.0, total_response_time = 0.0;
double in_q_time;
Pacchetto in_exec; /*Pacchetto che è in esecuzione al momento*/
int maxQ = -1;
int departureBig = 0, departureSmall = 0, arrival_big = 0, arrival_small = 0;

void scambia(int x, int y) {
  Pacchetto prov = queue[x];
  queue[x] = queue[y];
  queue[y] = prov;
}

Pacchetto extractMin() {  /*Estraggo il Pacchetto con size minore dalla coda*/
  num_in_q = num_in_q - 1;      /*decremento prima per non dover usare (num_in_q - 1) nel codice seguente */
  Pacchetto x = queue[0];
  queue[0] = queue[num_in_q];
  int i = 0;
  while (i < num_in_q && queue[i]->RPT > queue[i + 1]->RPT) {
    scambia(i, i + 1);
    i++;
  }
  return x;
}

void add(Pacchetto p) {   /*Aggiungo il pacchetto in modo ordinato nella coda*/
  int i = num_in_q;
  num_in_q = num_in_q + 1;
  maxQ = num_in_q > maxQ ? num_in_q : maxQ;
  if (num_in_q > Q_LIMIT) {
    printf("Queue overflow\n");
    exit(2);
  }
  queue[i] = p;
  while (i > 0 && queue[i]->RPT < queue[i - 1]->RPT) {
    scambia(i, i - 1);
    i--;
  }
}

/*Sample from exponential distribution*/
double exponential(double rate) { return -log(rand() * 1.0 / RAND_MAX) / rate; }

/*Initialize the simulation with empty queue*/
void initialize(void) {
  srand(time(NULL)); /*Not to be done in serious simulations*/

  sim_time = 0.0;

  /*model state*/
  server_status = IDLE;
  num_in_q = 0;

  for (int i = 0; i < Q_LIMIT; i++) {
    queue[i] = (Pacchetto)malloc(sizeof(struct pacchetto));
    queue[i]->RPT = -1;
  }

  in_exec = (Pacchetto)malloc(sizeof(struct pacchetto));

  /*statistics*/
  time_last_event = 0.0;
  area_num_in_q = 0.0;
  n_arrival = 0;

  /*Event list*/
  time_next_event[0] = sim_time + exponential(LAMBDA);
  time_next_event[1] = -1.0; /*negative denotes no event scheduled*/
}

/*Determine next event*/
void timing() {
  int found = 0;
  double min;
  int i;

  for (i = 0; i < NUMEVENT; i++)
    if (time_next_event[i] >= 0 && (!found || time_next_event[i] < min)) {
      min = time_next_event[i];
      next_event_type = i;
      found = 1;
    }

  if (!found) {
    printf("Simulation terminated because of empty event list\n");
    exit(1);
  } else{

    /* per misurare l'utilizzo */
    if (server_status == IDLE)
      idle_time += min - sim_time;
    else
      busy_time += min - sim_time;

    /* il simulatore avanza al prossimo evento */

    sim_time = min;
    in_exec->RPT = time_next_event[1]-sim_time;
  }
}

/*Handle arrival event*/
void arrival(Pacchetto packet) {
  if (packet->type == PACKET_BIG)
    arrival_big++;
  else
    arrival_small++;

  /*schedule next arrival*/
  time_next_event[0] = sim_time + exponential(LAMBDA); // schedula il prossimo arrival

  if (server_status == BUSY) {
    if (in_exec->RPT < packet->RPT)                 //Il nuovo pacchetto ha un tempo di esecuzione più lungo
      add(packet);                                  //Viene aggiunto alla coda il nuovo pacchetto
    else {
      add(in_exec);                                 /*Il pacchetto in esecuzione torna nella coda*/
      in_exec = packet;                             /*Il nuovo pacchetto appena arrivato diventa il pacchetto in esecuzione*/
      time_next_event[1] = sim_time + packet->RPT;  /*La prossima departure si farà al completamento del pacchetto, sempre se non ne arrivi uno più piccolo*/
    }
  } else {
    in_q_time = 0.0;                                /*Resetto il tempo in cui sono in coda (non ci sono mai stato)*/
    in_exec = packet;
    server_status = BUSY;
    /*schedule event for departure*/
    time_next_event[1] = sim_time + in_exec->RPT;
  }
}

/*Handle departure*/
void departure() {
  //statistics
  if (in_exec->type == PACKET_BIG)
    departureBig++;
  else
    departureSmall++;
  total_response_time += sim_time - in_exec->arrival_time;
  total_waiting_time += sim_time - in_exec->arrival_time - in_exec->type;
  if (num_in_q == 0) {                            /*none in queue!*/
    server_status = IDLE;
    time_next_event[1] = -1.0;                    /*no further departure*/
  } else {
    in_exec = extractMin();                       /*il pacchetto da eseguire è il minore in coda*/
    time_next_event[1] = sim_time + in_exec->RPT;
  }
}

/*Handle statistics*/
void statistics() {
  double delay;

  delay = sim_time - time_last_event;
  time_last_event = sim_time;

  area_num_in_q += num_in_q * delay;
}

int main() {
  int i, j;
  double occ;
  initialize();
  for (i = 0; i < NUM_MAX_EVENTS; i++) {
    timing();
    statistics();
    switch (next_event_type) {
      case 0:
        n_arrival++;
        Pacchetto p = (Pacchetto)malloc(sizeof(struct pacchetto));

        /* 80% pacchetti da 1024 , 20% pacchetti piccoli */
        occ = (rand() * 1.0 / RAND_MAX);
        p->RPT = occ >= RATE_SMALL ? PACKET_BIG : PACKET_SMALL;
        p->type = p->RPT;
        p->arrival_time = sim_time;

        /* pacchetto la cui dimensione segue una distribuzione esponenziale con rate MU
        p->RPT = exponential(MU);
        p->type = p->RPT;
        p->arrival_time = sim_time;
        */

        arrival(p);
        break;
      case 1:

        departure();
        break;
    }
  }

  //OUTPUT
  printf("Simtime: %f, Number of departure: %d\n", sim_time, NUM_MAX_EVENTS - n_arrival);
  printf("dbig: %d, dsmall: %d\n", departureBig, departureSmall);
  printf("abig: %d, asmall: %d\n", arrival_big, arrival_small);

  printf("The usage is: %f %% \n", (busy_time * 100) / (busy_time + idle_time));
  printf("The throughput is: %f unit of packet size/unit of simulation time (Max = 1)\n", (departureBig * PACKET_BIG + departureSmall * PACKET_SMALL) / sim_time);
  printf("The medium response time is: %f\n", total_response_time/ (departureBig+departureSmall));
  printf("The medium waiting time is: %f\n", total_waiting_time / (departureBig + departureSmall));
  printf("The expected number of customer is queue is: %f \n", area_num_in_q / sim_time);
  printf("Number of arrival: %d  Max number of packet in q:%d\n", n_arrival, maxQ);

  return 0;
}
