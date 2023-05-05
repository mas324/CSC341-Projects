import java.util.concurrent.Semaphore;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



/****************************************************/
public class SmokersProblem {

    // we wil needs some  random numbers 
    public static Random randy = new Random(1);   // seed so we always get same sequence 

    // Delay for times 
    public static final int Delay = 3000; // 2500;

    // Number of smokers
    private static final int NUM_SMOKERS = 3;

    // Smoking supply items 
    static enum Supplies { TOBACCO, PAPERS, MATCHES }
    static enum Enities { Tobacco, Papers, Matches, Agent }

    //Semaphores
    static Semaphore sem_tobacco = new Semaphore(0);
    static Semaphore sem_papers  = new Semaphore(0);  
    static Semaphore sem_matches = new Semaphore(0);
    static Semaphore sem_more_needed = new Semaphore(0);
    //boolean  tobacco, papers, matches;

    private static Lock lock = new ReentrantLock();
    //Condition[] condition = new Condition[NUM_SUPPLIES]; 


    private Smokers smokers[] = new Smokers[NUM_SMOKERS];
    private Agent agent;
    private LookForDeadlock dlcheck;
    private static ThreadSafePrinter screen;

    private static int smoked[] = new int[3];
        
    public SmokersProblem() {
        lock = new ReentrantLock();
        //smokers[] = new Smokers()[NUM_SMOKERS];
        for (int i = 0; i < NUM_SMOKERS; i++) { 
            Supplies missing = Supplies.values()[i%Supplies.values().length];
            smokers[i] = new Smokers(i, missing);
        }
        agent = new Agent();
        dlcheck = new LookForDeadlock(this);
        screen = new ThreadSafePrinter();
        screen.start();
        Semaphore sem_tobacco = new Semaphore(0);
        Semaphore sem_papers  = new Semaphore(0);
        Semaphore sem_matches = new Semaphore(0);
        Semaphore sem_more_needed = new Semaphore(0);
    }

    public Agent getAgent() { return agent; }

    public Smokers[] getSmokers() { return smokers; }



    public static void print_semaphores(Thread caller){
            screen.start_multi_print();
            screen.mprintln(caller+" sem_tobacco:"+sem_tobacco);
            screen.mprintln(caller+" sem_papers:"+sem_papers);
            screen.mprintln(caller+" sem_matches:"+sem_matches);
            screen.stop_multi_print();
    } 

    public static void print_locks(Thread caller){
            screen.start_multi_print();
            screen.mprintln(caller+" lock:"+lock);
            screen.stop_multi_print();
    } 

    public static void Sleep(int delay){
        try{
            Thread.sleep(delay);
        }catch (InterruptedException e){e.printStackTrace();} 
    }

    public static void wait (Semaphore sem){
        try{
               sem.acquire();
           } catch (InterruptedException e) { e.printStackTrace();}
    }
    public static void signal (Semaphore sem){
        try{
            sem.acquire();
        } catch (InterruptedException e) { e.printStackTrace();}
    }

    /************************************************************/
    // Smoker Thread 
    /************************************************************/
    public static class Smokers extends Thread  {
        private int id;
        private static int MAXROUNDS = 10;
        private Supplies have;                   // what I need TOBACCO, PAPERS, MATCHES
        private Enities type;
        private Supplies need_one, need_two;
        private Semaphore sem_one, sem_two;
        private volatile boolean need[] = new boolean[3];  //TOBACCO, PAPERS, MATCHES
        private volatile Supplies want[] = new Supplies[2];  //  TOBACCO,  PAPERS,  MATCHES
        private volatile Supplies holding[] = new Supplies[2];  //  TOBACCO,  PAPERS,  MATCHES
        private volatile boolean halt = false; 

        public Smokers(int id, Supplies have) {
            super(id%3==0?"Tobacco":id%3==1?"Papers ":"Matches"); 
            this.id = id;
            this.have = have;
            this.type = Enities.values()[have.ordinal()];
            for(int i=0;i<need.length;i++) 
               need[i] = true;
            need[id] = false;
            want[0] = want[1] = null;  // we do not initially want anything
            holding[0] = holding[1] = null;  // we do not initially hold anything

            switch (have) {
                case TOBACCO:
                    // TOBACCO, PAPERS, MATCHES
                    sem_one = sem_papers;
                    sem_two = sem_matches;
                    need_one=Supplies.PAPERS;  
                    need_two=Supplies.MATCHES;
                    break;
                case PAPERS:
                    // PAPERS, MATCHES TOBACCO
                    sem_one = sem_matches;
                    sem_two = sem_tobacco;
                    need_one=Supplies.MATCHES;
                    need_two=Supplies.TOBACCO;
                    break;
                default:  //case MATCHES:
                    //  MATCHES TOBACCO, PAPERS
                    sem_one = sem_tobacco;
                    sem_two = sem_papers;
                    need_one=Supplies.TOBACCO;
                    need_two=Supplies.PAPERS;
                }
            // /*DEBUG*/screen.println("created: "+info());
           want[0] = need_one;  // we do not currently have this 
           want[1] = need_two;  // we do not currently have this 
        }


        public void halt(){ 
            halt = true;
            MAXROUNDS = 0;
        }

        public String toString(){
            return super.getName();//type+"["+id+"]";
        }

        public boolean[] get_needs(){ 
            return need;
        }

        public boolean needs(Supplies item){ 
            return need[item.ordinal()];
        }

        private String need_string(){
            String ret = "";
            for (int i=0;i<need.length;i++){
                //ret += Supplies.values()[i].toString().toLowerCase()+"["+(need[i]?"TRUE":"false")+"] ";
                ret += (need[i])?Supplies.values()[i].toString().toLowerCase()+" ":"";
            }
            return ret;
        }

        // of the two item I may need have I asked either? 
        public Supplies wants(){
            if(want[0] != null)
               return want[0];
            else 
               return want[1]; 
        }

        // have I asked for this item?
        public boolean wants(Supplies item){
            Supplies i_want = wants();
            if(i_want != null && i_want == item) 
               return true; 
            return false;
        }

        // of the two item I may need I currently have? 
        public Supplies holding(){
            if(holding[0] != null)
               return holding[0];
            else
               return holding[1];
        }

        // have I asked for this item?
        public boolean holding(Supplies item){
            Supplies i_have = holding();
            if(i_have != null && i_have == item)
               return true;
            return false;
        }

        public String info(){
            String ret = this.toString() +" needs: ";//+Supplies.values()[id].toString();
            return ret + need_string();
        }



        public boolean my_items_are_out(){
            screen.println("  "+this.toString()+" checking if they can smoke ("+need_string()+")");
            if(sem_one.availablePermits() == 1 && sem_two.availablePermits() == 1)
                return true;
            return false;
        }

        public Enities whos_turn(){
            Supplies item_not_there = Agent.item_not_set_out();
            return Enities.values()[item_not_there.ordinal()];
        }

        public void ask_and_wait_for_first_item(){
            want[0]=need_one;
             /* DEBUG */screen.println(""+this.toString()+" looking for "+need_one);
            try{
                sem_one.acquire();
            } catch (InterruptedException e) { e.printStackTrace();}
            want[0]=null;
            holding[0]=need_one;
            need[need_one.ordinal()] = false;
            //screen.println("  "+this.toString()+" GOT "+need_one);
             /*DEBUG*/screen.println(" "+this.toString()+" GOT "+need_one+"\t\t\t\t"+this.info());
            // /* DEBUG */ print_semaphores(this);
        }
        public void ask_and_wait_for_second_item(){
           want[1]=need_two;
            /* DEBUG */screen.println(""+this.toString()+" looking for "+need_two);
           try{
               sem_two.acquire(); 
           } catch (InterruptedException e) { e.printStackTrace();}
           want[1]=null;
           holding[1]=need_two;
           need[need_two.ordinal()] = false;
           //screen.println("  "+this.toString()+" GOT "+need_two);
            /* DEBUG */ screen.println(" "+this.toString()+" GOT "+need_two+"\t\t\t\t\t"+this.info());
           // /* DEBUG */ print_semaphores(this);
        }

        void smoke_it(){
            // smoke 
            screen.start_multi_print();
            
            screen.mprintln("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%") ;
            screen.mprint  ("%        "+this.toString()+" is SMOKING                  %\n");
            for(int i=0;i<5;i++){
                 screen.mprint("%     ");
                 for(int j=0;j<i;j++) 
                     screen.mprint("       ");
                    for(int j=i;j<5;j++) 
                        screen.mprint("   puf ");
                        screen.mprintln("    %");
            }
            screen.mprintln("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%") ;
            
            screen.stop_multi_print();
            // done smoking 
        }

        void return_items(){
           // we wait to smoke and are missing these 
           need[need_one.ordinal()] = true;
           need[need_two.ordinal()] = true;
           holding[0] = holding[1] = null;  // we are no longer holding anything
           want[0] = need_one;  // we do not currently want this 
           want[1] = need_two;  // we do not currently want this 
           // /* DEBUG */screen.println("\t\t\t\t\t"+this.info());

           // tell the agent we are done 
           sem_more_needed.release();
        }


        /**************************************************************************/

        public void run() {
            // /*DEBUG*/screen.println(""+this.toString()+" starting   ");//+need_string());
            int rounds = 0;
            for (rounds=0; rounds<MAXROUNDS; rounds++){
                ask_and_wait_for_first_item();
                ask_and_wait_for_second_item();
                ///* DEBUG */screen.println("\t\t\t\t\t"+this.info());
            
                // smoke 
                smoke_it();

                return_items();

                //try { // WORKING 
                //    Thread.sleep(randy.nextInt(Delay)); 
                //} catch (InterruptedException e) { e.printStackTrace(); }
           }
           halt();
           smoked[type.ordinal()] = rounds;
           screen.println("  "+this+ " smoked " + rounds + " times.");
        }
        /**************************************************************************/
    }
    /************************************************************/
    // Smoke Thread 
    /************************************************************/



    /************************************************************/
    public static class Agent extends Thread  {
        private boolean open = true; 
        private static Supplies item_not_set_out = null;
  
        public Agent(){ super("Agent"); }

        public void halt(){ open = false; }

        public static Supplies item_not_set_out() {
            return item_not_set_out;
        }

        public void run() {
            // /*DEBUG*/screen.println("Agent starting ... ");
            while (open)    {
                int number = randy.nextInt(3);// % 3;
                switch (number)        {
                    case 0: 
                         screen.println("/*****************************************/");
                         screen.println("    Agent put out MATCHES and PAPERS ");
                         screen.println("/*****************************************/");
                         sem_matches.release(); 
                         sem_papers.release();
                         item_not_set_out = Supplies.TOBACCO;
                         // /* DEBUG */ print_semaphores(this);
                         /* match and paper */
                         break;
                    case 1: 
                         screen.println("/*****************************************/");
                         screen.println("    Agent put out MATCHES and TOBACCO ");
                         screen.println("/*****************************************/");
                         sem_matches.release(); 
                         sem_tobacco.release();
                         item_not_set_out = Supplies.PAPERS;
                         // /* DEBUG */ print_semaphores(this);
                         /* match and tobacco */
                         break;
                    case 2: 
                         screen.println("/*****************************************/");
                         screen.println("    Agent put out PAPERS and TOBACCO  ");
                         screen.println("/*****************************************/");
                         sem_papers.release();
                         sem_tobacco.release();
                         item_not_set_out = Supplies.MATCHES;
                         // /* DEBUG */ print_semaphores(this);
                         /* tobacco and paper */
                         break;
                }
                
                try{
                    
                    sem_more_needed.acquire();    /* wait for request for more */
                    // /* DEBUG */ print_semaphores(this);
                    
                } catch (InterruptedException e) { 
                    //e.printStackTrace(); 
                    screen.println(""+this.getName()+" has recieve a message to shutdown");    
                    halt();
                }     
            }

        }

    }  
    /************************************************************/

    /************************************************************/
    public static class LookForDeadlock extends Thread  {
        private boolean keepRunning =  true;
        private int delay = 9000; 
        private Smokers smokers[] = new Smokers[NUM_SMOKERS];
        private Agent agent = null;
        private final int TOBACCO = Supplies.TOBACCO.ordinal();
        private final int PAPERS = Supplies.PAPERS.ordinal();
        private final int MATCHES = Supplies.MATCHES.ordinal();
        public Supplies wants[] = new Supplies[NUM_SMOKERS];
        public Enities holding_and_waiting[][] = new Enities[NUM_SMOKERS][2];
        
        public LookForDeadlock(SmokersProblem sp) {
            
            super("DeadLockChecker");

            SetThreads(sp.getSmokers());

            agent = sp.getAgent();

            for(int i=0;i<NUM_SMOKERS;i++) {
                wants[i] = null;
                holding_and_waiting[i][0] = null;
                holding_and_waiting[i][1] = null;
            }
        }

        public void set_delay(int d){ delay=d; }

        public void SetThreads( Smokers lungers[] ){
            for(int i=0;i<NUM_SMOKERS; i++)
                smokers[i]=lungers[i];
        }

        public void find_holds_and_waits(){
            for (int i=0; i<NUM_SMOKERS; i++){
                if(smokers[i].holding() != null)
                    holding_and_waiting[i][0]=Enities.values()[smokers[i].holding().ordinal()];
                else 
                    holding_and_waiting[i][0] = null;
                if(smokers[i].wants() != null) 
                    holding_and_waiting[i][1]=Enities.values()[smokers[i].wants().ordinal()];
                else
                    holding_and_waiting[i][1] = null;
            }
        }

        public String items_put_out(){
            switch (agent.item_not_set_out()){
                case TOBACCO: return " PAPERS and MATCHES";
                case PAPERS: return " TOBACCO and MATCHES";
                case MATCHES: return " TOBACCO and PAPERS";
            }
            return "";
        }

        public void print_holds_and_waits(){
            find_holds_and_waits();
            screen.println("##############################################");
            screen.println("Agent has put out "+items_put_out());
            for (int i=0; i<NUM_SMOKERS; i++){
                screen.println(smokers[i]+" is holding "+holding_and_waiting[i][0]
                        + " while waiting for "
                        +holding_and_waiting[i][1].toString().toUpperCase());
            }
            screen.println("##############################################");
        }

        public void find_wants(){
            for (int i=0; i<NUM_SMOKERS; i++) 
                wants[i]=smokers[i].wants();
        }

        public void print_wants(){
            find_wants();
            screen.start_multi_print();
            screen.mprintln("+++++++++++++++++++++++++++++++++++++++++++++++");
            for (int i=0; i<NUM_SMOKERS; i++){
                screen.mprintln(smokers[i]+" wants "+ wants[i]);
            }
            screen.mprintln("+++++++++++++++++++++++++++++++++++++++++++++++");
            screen.stop_multi_print();
        }

        public int print_blocked(){ return blocked(true); }
        public int blocked(){ return blocked(false); }
        public int blocked(boolean print){
            Supplies item_not_out = agent.item_not_set_out();
            Enities blocked[] = new Enities[3]; // Smoker i is blocked on Supply at [i];
            int blocked_count = 0;
            boolean cycle = false;
            boolean more = false;

            find_holds_and_waits();
            for(int i = 0; i < NUM_SMOKERS; i++){
              for(int j=0; j < NUM_SMOKERS; j++){
                    if(holding_and_waiting[i][1] == Enities.values()[item_not_out.ordinal()]){
                        holding_and_waiting[i][0] = Enities.Agent;
                        blocked[i] = Enities.Agent;
                    }
                   // smoker[i] is holding somethign smoker[j] is waiting on
                   if( holding_and_waiting[i][1] == holding_and_waiting[j][0] ){
                       blocked[i] = Enities.values()[j];
                       // smoker[j] is then blocked by smoker[i]
                   }
                   if(j==0 && holding_and_waiting[i][1] != null && holding_and_waiting[i][0] != null)
                       blocked_count++;
               }
               if(blocked[i] == Enities.Agent || blocked[i] ==null ){
                   more=false;
               }else
                   more = true;
            }
            screen.start_multi_print();
            if(print)screen.mprintln("");
            if(print)screen.mprintln("########################################################");
            for(int i = 0; i < NUM_SMOKERS; i++){
                if(holding_and_waiting[i][1] != null) {
                    String tmp = (blocked[i]==Enities.Agent)
                        ?" Agent, has not put out wanted item ("+holding_and_waiting[i][1]+")."
                        : blocked[i]+", which is holding wanted item ("+holding_and_waiting[i][1]+").";
                    if(i==0 && print) screen.mprint("TOBACCO blocked by "+tmp+"\n");
                    if(i==1 && print) screen.mprint("PAPERS  blocked by "+tmp+"\n");
                    if(i==2 && print) screen.mprint("MATCHES blocked by "+tmp+"\n");
                }
            }
            if(print)screen.mprintln("########################################################");
            if(print)screen.mprintln("");
            screen.stop_multi_print();
            
            return blocked_count;
        }

        public boolean  check(){
            int blocked_count = 0;
           
            // Hold and Wait 
            //for(int i = 0; i < NUM_SMOKERS; i++){
            //    if(holding_and_waiting[i][0] != null){  // make sure smoker is waiting for something 
            //        hold_and_wait_count++;
            //    }
            //}
            //
            // the above is not good enough ie we could have a hold and wait and not have deadlock 
            // if we have 2 or more hold and wait then we have deadlock 
            // but while it is sufficient, it is not necessary
            //
              
            // How many are Blocked?
            blocked_count = blocked();
    
            boolean exit=false;

            if( blocked_count >=  3) {   
                // /*DEBUG*/screen.start_multi_print();
                // /*DEBUG*/screen.mprintln("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                // /*DEBUG*/screen.mprintln("             found "+blocked_count+" blocked ");
                // /*DEBUG*/screen.mprintln("                 DEADLOCK");
                // /*DEBUG*/screen.mprintln("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                // /*DEBUG*/screen.mprintln("");
                // /*DEBUG*/screen.stop_multi_print();
                exit = true;
            }
            if( cycle() ){
                // /*DEBUG*/screen.start_multi_print();
                // /*DEBUG*/screen.mprintln("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                // /*DEBUG*/screen.mprintln("              found CYCLE(s) ");
                // /*DEBUG*/screen.mprintln("               DEADLOCK");
                // /*DEBUG*/screen.mprintln("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                // /*DEBUG*/screen.mprintln("");
                // /*DEBUG*/screen.stop_multi_print();
                exit = true;

            }
            

            if(exit){
                
                return true;
            }
            return false;
        }

 
        public boolean print_cycle( ) { return cycle(true);}
        public boolean cycle( ) { return cycle(false);}
        public boolean cycle( boolean print ) {
            Supplies item_not_out = agent.item_not_set_out();
            Enities blocked[] = new Enities[3]; // Smoker i is blocked on Supply at [i];
            find_holds_and_waits(); 
            if(print) screen.start_multi_print();
            boolean cycle = false;
            boolean more = false;
            if( print ) screen.mprintln("") ;
            if( print ) screen.mprintln("##############################################");
            if( print ) screen.mprintln("              WAIT FOR CYCLE(s)               ") ;
            if( print ) screen.mprintln("          who is waiting from whom            ") ;
            if( print ) screen.mprintln("                                              ") ;
            if( print ) screen.mprintln("   if two smokers are waitting for the agent  ");
            if( print ) screen.mprintln("    then agent is also waitting for smokers   ");
            for(int i = 0; i < NUM_SMOKERS; i++){
               if(!more && print) 
                   screen.mprint("     "+(Enities.values()[i])) ;
               for(int j=0; j < NUM_SMOKERS; j++){
                    if(holding_and_waiting[i][1] == Enities.values()[item_not_out.ordinal()]){
                        holding_and_waiting[i][0] = Enities.Agent;
                        blocked[i] = Enities.Agent;
                    }
                   // smoker[i] is holding somethign smoker[j] is waiting on
                   if( holding_and_waiting[i][1] == holding_and_waiting[j][0] ){
                       blocked[i] = Enities.values()[j];
                       // smoker[j] is then blocked by smoker[i]
                   }
               }
               if( print ) screen.print(" --> "+blocked[i]);
               if(blocked[i] == Enities.Agent || blocked[i] ==null ){
                   if( print ) screen.mprintln("");
                   more=false;
               }else
                   more = true;
            }

            for(int i = 0; i < NUM_SMOKERS; i++){
                if( blocked[i] == null )
                    continue;
                if(blocked[i] == Enities.Agent ){ 
                    continue;
                }
                if( blocked[blocked[i].ordinal()] != null ){
                    if(blocked[blocked[i].ordinal()] == Enities.Agent){
                         cycle = true;
                         continue;
                    }
                }
                if( blocked[blocked[blocked[i].ordinal()].ordinal()] != null ){
                    if(blocked[blocked[i].ordinal()] == Enities.Agent){
                        cycle = true;
                        continue;
                    }
                }
            }
            if( print ) screen.mprintln("##############################################");
            if( print ) screen.mprintln("") ;
            if(print) screen.stop_multi_print();
            return cycle;
        }

        public void halt(){keepRunning=false;}
        public void run() {
            while(keepRunning) {
                try {
                    Thread.sleep(delay/2);
                } catch (InterruptedException e) { e.printStackTrace(); }

                ///*DEBUG*/print_wants();     
                ///*DEBUG*/print_holds_and_waits();
                if( check() ) { 
                    screen.println("\n") ;
                    print_holds_and_waits();
                    if(cycle()) print_cycle();
                    else  print_blocked();
                    halt();
                    System.exit(1);
                } 
            }
        }
    }
    /************************************************************/

    /************************************************************/
    /************************************************************/
    public static void main(String[] args) {
        SmokersProblem sp = new SmokersProblem();  //TOBACCO, PAPERS, MATCHES

        // Start agent
        sp.agent.start();

        // make sure angent is fully satup before we are grabbing item of his table     
        try { 
           Thread.sleep(sp.Delay/3);
        } catch (InterruptedException e) { e.printStackTrace(); }


        // Start smokers
        for (int i = NUM_SMOKERS-1; i>=0;  i--) {
           sp.smokers[i].start();
        }
        //
        sp.dlcheck.start();
       
        for(int i = 0; i < NUM_SMOKERS; i++) {
           try{
               sp.smokers[i].join();
           } catch (InterruptedException e){ e.printStackTrace(); }
            
           // once one smoke has finished - system stop working 
           // tell agent to close shop 
          
        }
        screen.println(" All smokers threads have finished "); 
        for(int i = 0; i < NUM_SMOKERS; i++) 
            screen.println(" "+Enities.values()[i]+" smoked "+smoked[i]+" times");
        // Agent is likely waitting the 'sem_more_needed' semaphore
        // so interupt them  (will be caught in try block
        sp.agent.interrupt();

        // stop deadlock detector  
        sp.dlcheck.halt();
        // stop screen thread 
        sp.screen.halt();

        try{  // screen thread shutdown so use System.out.print
            sp.agent.join();
            System.out.println(" Agent thread done ");
            sp.dlcheck.join();
            System.out.println(" deadlock checker done ");
            sp.screen.join();
            System.out.println(" screen thread done ");
        } catch (InterruptedException e){ e.printStackTrace(); }
   }
   /************************************************************/
   /************************************************************/

   /************************************************************/
   /************************************************************/
    //public static class Smokers extends Thread  
    public static class ThreadSafePrinter extends Thread {
        public volatile static boolean PrintLocked = false;
        public volatile static boolean MultiPrintLocked = false;
        private static Lock print_lock = new ReentrantLock();

        public void halt(){/*stop(); calling stop works but is deprecated*/}

        private static void PRINT(String str){
            System.err.print(""+str);
        }

        public static void print(String str){
            print_lock.lock();
            PRINT(str);
            print_lock.unlock();
        }

        public static void println(String str){
            print(str+"\n");
        }

        public static void start_multi_print(){
            print_lock.lock();
            MultiPrintLocked=true;
        }

        public static void stop_multi_print(){
            print_lock.unlock();
            MultiPrintLocked=false;
        }

        public static void mprint(String str){
            if(!MultiPrintLocked){
                System.err.print("Need to use start_multi_print and stop start_multi_print");
                return;
            }
            print(str);
        }

        public static void mprintln(String str){
            mprint(str+"\n");
        }

    }

   /************************************************************/
   /************************************************************/



}
/****************************************************/
/****************************************************/
/****************************************************/
/****************************************************/
/****************************************************/
