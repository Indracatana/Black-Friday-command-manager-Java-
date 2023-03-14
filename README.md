# Black-Friday-command-manager-Java-
• Designed two levels of threads for the implementation 
• Used Replicated Workers Model for processing orders in parallel, respectively the products from an order in parallel

------------------------------------------------------------------------------------------------------------------

• pentru task-urile de nivel 1, thread-urile se vor ocupa de citirea comenzilor si
adaugarea a cate nr_produse task-uri pentru fiecare comanda in pool-ul de task-uri de 
nivel 2. Constructorul pentru clasa ce prelucreaza task-urile de nivel 1 va avea ca 
argumente BufferedReader-ul pt fisierul de intrare pentru comenzi, BufferedWriter-ul 
pt fisierele de iesire pentru comenzi si produse, pool-ul cu task-uri de nivel 1, 
pool-ul cu task-uri de nivel 2, si cele 2 cozi pentru task-urile nivel 1, respectiv 2.
Citirea comenzilor se realizeaza cu un BufferedReader comun thread-urilor 
pentru a ma asigura ca 2 thread-uri nu citesc aceeasi linie(thread safe). Pentru a ma
asigura ca toate produsele dintr-o comanda au fost expediate inainte de a marca
comanda ca fiind expediata, folosesc cate un semafor pentru fiecare comanda initializat 
cu nr_produse, pe care il trimit ca argument thread-urilor de nivel 2. Astfel, dupa 
executarea task-ului sau, thread-ul de nivel 2 va da release la semaforul specific comenzii
din care facea parte produsul prelucrat, iar semaforul se va debloca abia dupa ce
nr_produse din comanda au fost prelucrate.

• pentru task-urile de nivel 2, thread-urile se vor ocupa de prelucarea unui produs pe 
rand. Acestea vor primi de la thread-urile de nivel 1 indicele produsului din comanda 
si numele comenzii de care trebuie sa se ocupe (pentru a asigura astfel ca prelucrarea
produselor se realizeaza in ordine), BufferedWriter-ul pentru fisierul de iesire pt 
produse, pool-ul cu task-uri de nivel 2, coada cu task-uri de nivel 2 si semaforul 
initializat cu numarul de produse ale comenzii. Fiecare thread va folosi un 
BufferedReader propriu, iar atunci cand a ajuns la numarul produsului ce i-a fost atribuit,
il va marca ca fiind expediat si va da release semaforului propriu comenzii din care 
face parte produsul.


