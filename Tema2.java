import java.io.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Tema2 {
    public static int nr_max_threads;
    public static String folder_input;

    public static void main(String args[]) {
        //citire argumente din linia de comanda
        folder_input = args[0]; //nume folder input
        nr_max_threads = Integer.parseInt(args[1]);  //nr max thread-uri

        //pt task-uri nivel 1
        AtomicInteger inQueue = new AtomicInteger(0);
        ExecutorService tpe = Executors.newFixedThreadPool(nr_max_threads);
        //pt task-uri nivel 2
        AtomicInteger inQueue_prod = new AtomicInteger(0);
        ExecutorService tpe_prod = Executors.newFixedThreadPool(nr_max_threads);

        try {
            FileReader order = new FileReader(folder_input + "/orders.txt");
            BufferedReader sc_order = new BufferedReader(order);
            //fisier de iesire comenzi
            BufferedWriter writer_order = new BufferedWriter(new FileWriter("orders_out.txt"));
            //fisier de iesire produse
            BufferedWriter writer_product = new BufferedWriter(new FileWriter("order_products_out.txt"));
            //introduce task in pool
            inQueue.incrementAndGet();
            tpe.submit(new MyRunnable1(sc_order, writer_order, writer_product, tpe, tpe_prod, inQueue, inQueue_prod));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//Executor Service pentru task-urile de nivel 1
class MyRunnable1 implements Runnable {
    private final BufferedReader sc_order;
    private final BufferedWriter writer_order;
    private final BufferedWriter writer_product;
    private final ExecutorService tpe;
    private final ExecutorService tpe_prod;
    private final AtomicInteger inQueue;
    private final AtomicInteger inQueue_prod;

    //constructorul clasei
    //voi avea ca argumente BufferedReader-ul pt fisierul de intrare pentru comenzi, BufferedWriter-ul pt fisierele
    //de iesire pentru comenzi si produse, pool-ul cu task-uri de nivel 1, pool-ul cu task-uri de nivel 2,
    //si cele 2 cozi pentru task-urile nivel 1, respectiv 2
    public MyRunnable1(BufferedReader sc_order, BufferedWriter writer_order, BufferedWriter writer_product,
                       ExecutorService tpe, ExecutorService tpe_prod, AtomicInteger inQueue, AtomicInteger inQueue_prod) {
        this.sc_order = sc_order;
        this.writer_order = writer_order;
        this.writer_product = writer_product;
        this.tpe = tpe;
        this.inQueue = inQueue;
        this.inQueue_prod = inQueue_prod;
        this.tpe_prod = tpe_prod;
    }

    @Override
    public void run() {
        try {
            while (sc_order.ready()) {
                //citire din fisierul de intrare comenzi
                String comanda = sc_order.readLine();
                String[] result = comanda.split(","); //extragere nume comanda si nr produse
                int nr_produse = Integer.parseInt(result[1]);
                //daca nu am comanda de tip Empty Order
                if (nr_produse > 0) {
                    Semaphore sem = new Semaphore(nr_produse); //initializare semafor cu nr produse
                    try {
                        sem.acquire(); //asteapta la semafor
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    inQueue_prod.addAndGet(nr_produse); //adaug nr produse la coada de nivel 2
                    for (int i = 1; i <= nr_produse; i++)
                        //adaug task-urile de nivel 2 in pool
                        tpe.submit(new MyRunnable2(writer_product, i, result[0], tpe_prod, inQueue_prod, sem));
                    //dupa ce nr_produse au fost livrate si comanda poate fi livrata
                    writer_order.append(comanda + ",shipped\n");
                }
            }
            sc_order.close();  //inchid BufferedReader
        } catch (IOException e) {
            e.printStackTrace();
        }
        //oprire executie Executor Service
        int left = inQueue.decrementAndGet();
        if (left == 0) {
            tpe.shutdown();
            try {
                //inchidere fisier de iesire
                writer_order.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

//Executor Service pentru task-urile de nivel 2
class MyRunnable2 implements Runnable {
    private final BufferedWriter writer_product;
    private final int nr_produs;
    private final String nume_comanda;
    private final ExecutorService tpe_prod;
    private final AtomicInteger inQueue_prod;
    private final Semaphore sem;

    //constructorul clasei
    //contine BufferedWriter-ul pentru fisierul de iesire pt produse, indicele produsului din comanda respectiva,
    //numele comenzii, pool-ul cu task-uri de nivel 2, coada cu task-uri de nivel 2 si semaforul initializat cu numarul
    //de produse ale comenzii
    public MyRunnable2(BufferedWriter writer_product, int nr_produs, String nume_comanda, ExecutorService tpe_prod, AtomicInteger inQueue_prod, Semaphore sem) {
        this.writer_product = writer_product;
        this.nr_produs = nr_produs;
        this.nume_comanda = nume_comanda;
        this.inQueue_prod = inQueue_prod;
        this.tpe_prod = tpe_prod;
        this.sem = sem;
    }

    @Override
    public void run() {

        try {
            //citire din fisierul de intrare produse
            FileReader products = new FileReader(Tema2.folder_input + "/order_products.txt");
            BufferedReader sc_product = new BufferedReader(products);
            int ind = 0; //indice care retine la al cate-lea produs din comanda ma aflu
            while (sc_product.ready()) {
                String produs = sc_product.readLine();
                String[] result = produs.split(","); //extragere nume comanda si nume produs
                if (result[0].equals(nume_comanda)) {
                    ind++;
                    //daca am ajuns la indicele dorit al produsului din comanda il pot marca ca fiind livrat
                    if (ind == nr_produs) {
                        sem.release(); //dupa efectuarea task-ului, thread-ul da release la semafor
                        writer_product.append(produs + ",shipped\n");
                    }
                }
            }
            sc_product.close();  //inchid BufferedReader
        } catch (IOException e) {
            e.printStackTrace();
        }
        int left = inQueue_prod.decrementAndGet();
        //oprire executie Executor Service
        if (left == 0) {
            tpe_prod.shutdown();
            try {
                //oprire citire din fisier de iesire
                writer_product.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}