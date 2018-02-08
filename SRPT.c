
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

#define Q_LIMIT 15000 /*queue capacity*/
#define NUM_MAX_EVENTS 20000000
#define BUSY 1
#define IDLE 0

#define NUMEVENT 2

/*Parameters*/
#define LAMBDA 0.6
#define MU 3.0

typedef struct pacchetto {
  double RPT;
  double arrival_time;
} * Pacchetto;


double sim_time;                  /*simulated time*/
int server_status;                /*Is the server busy or idle?*/
int num_in_q;                     /*number of jobs in queue*/
double time_next_event[NUMEVENT]; /*event calendar. position 0 arrivals,
position 1 departures*/
int next_event_type;              /*type of next event, 0 arrival, 1 departure*/
Pacchetto queue[Q_LIMIT];

/*for the statistics*/
double idle_time; /* per l'utilizzo */
double busy_time; /* per l'utilizzo */
double time_last_event; /*epoch of last processed event*/
double area_num_in_q;   /*to compute the expected number of customers*/
int n_arrival;
double total_service_time = 0.0;
double in_q_time;
Pacchetto in_exec; /*Pacchetto che è in esecuzione al momento*/
int maxQ = -1;
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
  p->arrival_time = sim_time; /*IMPORTANTE : Sono entrato nella coda, setto il mio arrival_time
                               *ogni volta che lo faccio (problema 1)*/
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
  /*schedule next arrival*/
  time_next_event[0] = sim_time + exponential(LAMBDA);

  if (server_status == BUSY) {

    if (in_exec->RPT < packet->RPT) {
      add(packet);
    } else {

      total_service_time -= in_q_time;                 /*Ritornando in coda il tempo che aspetto può variare*/
      
      add(in_exec);                                 /*Il pacchetto in esecuzione torna nella coda*/
      in_exec = packet;                            /*Il nuovo pacchetto appena arrivato diventa il pacchetto in esecuzione*/
      time_next_event[1] = sim_time + packet->RPT;/*La prossima departure si farà al completamento del pacchetto, sempre se non ne arrivi uno più piccolo*/
    }
  } else {
    in_q_time = 0.0;                                   /*Resetto il tempo in cui sono in coda (non ci sono mai stato)*/

    in_exec = packet;
    server_status = BUSY;
    /*schedule event for departure*/
    time_next_event[1] = sim_time + in_exec->RPT;    
  }
}

/*Handle departure*/
void departure() {
  if (num_in_q == 0) { /*none in queue!*/
    server_status = IDLE;
    time_next_event[1] = -1.0; /*no further departure*/
  } else {
    in_exec = extractMin();                       /*il pacchetto da eseguire è il minore in coda*/
    time_next_event[1] = sim_time + in_exec->RPT;

    in_q_time = sim_time - in_exec->arrival_time; /*calcolo il tempo che sono stato in coda */
    total_service_time += in_q_time;              /*aggiungo al tempo totale in passato in coda il mio tempo (del pacchetto in questione)*/
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
  int i, j, nBig;
  double occ;
  nBig = 0;
  initialize();
  for (i = 0; i < NUM_MAX_EVENTS; i++) {

    timing();

    statistics();

    /*[Tipo Evento] Tempo rimanente al momento (Tempo rimanente) (Tempo di arrivo nella coda)   Tempo di simulazione   Sommatoria tempo passato in coda*/
    //printf("[%d] %f (%f) (%f)  sim_time:%f  total_service_time:%f\n", next_event_type,
    //       (time_next_event[1] - sim_time) >= 0.0 ? (time_next_event[1] - sim_time) : 0.0,  /*se ho una departure vuota scrive tutti 0*/
      //     in_exec->RPT, in_exec->arrival_time, sim_time, total_service_time);
    //for (j = 0; j < num_in_q; j++)
    //  printf("[(%f)  (%f)] ", queue[j]->RPT, queue[j]->arrival_time);
    //if (next_event_type != 0 || j != 0)
    //  printf("\n\n");

    switch (next_event_type) {
    case 0: { /*Nel caso di un arrival preparo un nuovo pacchetto e setto l'arrival time
              *ad adesso (NON è il tempo in cui sono entrato in coda, verrà settato nella add())*/
      n_arrival++;
      Pacchetto p = (Pacchetto)malloc(sizeof(struct pacchetto));

      /* pacchetto tipo 1 */
      occ = (rand() * 1.0 / RAND_MAX);
      nBig += occ >= 0.3 ? 1 : 0;
      p->RPT = occ >= 0.3 ? 2.16 : 0.5;
      

      /* pacchetto tipo 2
      p->RPT=(exponential(MU));
      */

      p->arrival_time = sim_time;
      //printf("arrivato %f\n\n", p->RPT);
      arrival(p);
      break;
    }
    case 1:
      departure();
      break;
    }
  }
  printf("The usage is %f %% \n",busy_time*100/(busy_time+idle_time));
  printf("The throughput is ");
  printf("The expected number of customer is queue is %f \n", area_num_in_q / sim_time);
  printf("The medium waiting time is %f\n", total_service_time / (n_arrival - num_in_q));
  printf("%d %d %d\n", n_arrival, nBig, maxQ);
  return 0;
}
