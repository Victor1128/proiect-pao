package Services;

import java.time.LocalDate;
import java.util.*;

import Models.Materie.Materie;
import Models.Ora.Curs;
import Models.Ora.Laborator;
import Models.Ora.Ora;
import Models.Ora.Seminar;
import Models.Profesor.Profesor;
import Models.Sala.Amfiteatru;
import Models.Sala.Sala;
import Models.Sala.SalaLaborator;
import Models.Sala.SalaSeminar;
import Models.Serie.Serie;
import Models.Student.Student;
import utils.ClassWithName;

public class MainService {
    private List<Student> studenti = new ArrayList<>();
    private List<Profesor> profesori = new ArrayList<>();
    private List<Sala> sali = new ArrayList<>();
    private List<Materie> materii = new ArrayList<>();
    private List<Serie> serii = new ArrayList<>();
    private Map<String, List<Ora>> grupaOreMap = new HashMap<>();
    private Map<Profesor, List<Ora>> profesorOreMap = new HashMap<>();
    private List<Ora> ore = new ArrayList<>();
    private final List<String> zileleSaptamanii = Arrays.asList("Luni", "Marti", "Miercuri", "Joi", "Vineri", "Sambata", "Duminica");

    private void addOraToMaps(Profesor p, String grupa) {
        if(!grupaOreMap.containsKey(grupa))
            grupaOreMap.put(grupa, new ArrayList<>());
        grupaOreMap.get(grupa).add(ore.get(ore.size() - 1));
        if(!profesorOreMap.containsKey(p))
            profesorOreMap.put(p, new ArrayList<>());
        profesorOreMap.get(p).add(ore.get(ore.size() - 1));
    }
    public void createStudent(Scanner in){
        Student student = new Student(in);
        studenti.add(student);
        System.out.println("Studentul a fost creat cu succes!");
    }

    public void createProfesor(Scanner in){
        Profesor profesor = new Profesor(in, materii);
        profesori.add(profesor);
        System.out.println("Profesorul a fost creat cu succes!");
    }

    public void createSerie(Scanner in) {
        Serie serie = new Serie(in);
        serii.add(serie);
        System.out.println("Serie creata cu succes!");
    }

    public void createMaterie(Scanner in){
        Materie materie = new Materie(in, "Simple");
        materii.add(materie);
        System.out.println("Materia a fost creata cu succes!");
    }

    public void createSala(Scanner in){

        int optiune = -1;
        while(optiune < 0 || optiune > 2) {
            System.out.println("Tip (0 - amfiteatru, 1 - laborator, 2 - seminar):");
            optiune = Integer.parseInt(in.nextLine());
        }
        switch (optiune) {
            case 0 -> sali.add(new Amfiteatru(in));
            case 1 -> sali.add(new SalaLaborator(in, materii));
            case 2 -> sali.add(new SalaSeminar(in));
        }
        System.out.println("Sala a fost creata cu succes!");

    }

    public void createOra(Scanner in){
        int tip = -1;
        while(tip < 0 || tip > 2) {
            System.out.println("Tip (0 - curs, 1 - laborator, 2 - seminar):");
            tip = Integer.parseInt(in.nextLine());
        }
        switch (tip){
            case 0 -> {
                Curs curs = new Curs(in, profesori, materii, sali, serii);
                ore.add(curs);
                for (String g : curs.getSerie().getGrupe()) {
                    if(!grupaOreMap.containsKey(g))
                        grupaOreMap.put(g, new ArrayList<>());
                    grupaOreMap.get(g).add(ore.get(ore.size() - 1));
                }
                if(!profesorOreMap.containsKey(curs.getProfesor()))
                    profesorOreMap.put(curs.getProfesor(), new ArrayList<>());
                profesorOreMap.get(curs.getProfesor()).add(ore.get(ore.size() - 1));
            }
            case 1 -> {
                Laborator laborator = new Laborator(in, profesori, materii, sali);
                ore.add(laborator);
                addOraToMaps(laborator.getProfesor(), laborator.getGrupa());
            }
            case 2 -> {
                Seminar seminar = new Seminar(in, profesori, materii, sali);
                ore.add(seminar);
                addOraToMaps(seminar.getProfesor(), seminar.getGrupa());
            }
            }
        System.out.println("Ora a fost adaugata cu succes!");
    }

    public Student findStudentById(Long id){
        for (Student s : studenti) {
            if(Objects.equals(s.getId(), id))
                return s;
        }
        return null;
    }

    public Student findStudentByName(String name){
        int cnt = 0, index = -1;
        for (Student s : studenti) {
            if(s.getName().equals(name))
            {
                cnt++;
                index = studenti.indexOf(s);
            }
        }
        if(cnt == 0)
        {
            System.out.println("Nu exista niciun student cu acest nume!");
            return null;
        }
        if(cnt == 1)
            return studenti.get(index);
        System.out.println("Exista mai multi studenti cu acest nume!");
        return null;
    }

    public Profesor findProfesorById(Long id){
        for (Profesor p : profesori) {
            if(Objects.equals(p.getId(), id))
                return p;
        }
        return null;
    }

    public Profesor findProfesorByName(String name){
        int cnt = 0, index = -1;
        for (Profesor p : profesori) {
            if(p.getName().equals(name))
            {
                cnt++;
                index = profesori.indexOf(p);
            }
        }
        if(cnt == 0)
            System.out.println("Nu exista niciun profesor cu acest nume!");
        else if(cnt == 1)
            return profesori.get(index);
        else System.out.println("Exista mai multi profesori cu acest nume!");
        return null;
    }

    public void getStudent(Scanner in){
        System.out.println("Introduceti numele studentului: ");
        String nume = in.nextLine();
        System.out.println(findStudentByName(nume));
    }

    public void getProfesor(Scanner in){
        System.out.println("Introduceti numele profesorului: ");
        String nume = in.nextLine();
        System.out.println( findProfesorByName(nume));
    }
    public void afisareOrarStudent(Scanner in){
        System.out.println("Introduceti numele studentului: ");
        String nume = in.nextLine();
        Student s = findStudentByName(nume);
        if(s == null){
            System.out.println("Introduceti id-ul studentului: ");
            Long id = in.nextLong();
            s = findStudentById(id);
            if(s == null){
                System.out.println("Nu exista niciun student cu id!");
                return;
            }
        }
        String grupa = s.getGrupa();
        Collections.sort(grupaOreMap.get(grupa));
        int zs = -1;
        for (Ora o : grupaOreMap.get(grupa)) {
            if(o.getZiuaSaptamanii() != zs){
                zs = o.getZiuaSaptamanii();
                System.out.println("\n\n" + zileleSaptamanii.get(zs)+":");
            }
            System.out.println(o);
        }

    }

    public void afisareOrarProfesor(Scanner in){
        System.out.println("Introduceti numele profesorului: ");
        String nume = in.nextLine();
        Profesor p = findProfesorByName(nume);
        if(p == null){
            System.out.println("Introduceti id-ul profesorului: ");
            Long id = in.nextLong();
            p = findProfesorById(id);
            if(p == null){
                System.out.println("Nu exista niciun profesor cu id!");
                return;
            }
        }
        Collections.sort(profesorOreMap.get(p));
        int zs = -1;
        for (Ora o : profesorOreMap.get(p)) {
                if(o.getZiuaSaptamanii() != zs){
                    zs = o.getZiuaSaptamanii();
                    System.out.println("\n\n" + zileleSaptamanii.get(zs)+":");
                }
                System.out.println(o.toStringProfesor());
        }

    }

    public void afisareOrarStudentZiCurenta(Scanner in){
        System.out.println("Introduceti numele studentului: ");
        String nume = in.nextLine();
        Student s = findStudentByName(nume);
        if(s == null){
            System.out.println("Introduceti id-ul studentului: ");
            Long id = in.nextLong();
            s = findStudentById(id);
            if(s == null){
                System.out.println("Nu exista niciun student cu id!");
                return;
            }
        }
        String grupa = s.getGrupa();
        int ziuaCurenta = LocalDate.now().getDayOfWeek().getValue()-1;
        List<Ora> ore = grupaOreMap.get(grupa).stream().filter(o -> o.getZiuaSaptamanii() == ziuaCurenta).sorted().toList();
        for (Ora o : ore) {
            System.out.println(o);
        }
    }

    public void afisareOrarProfesorZiCurenta(Scanner in){
        System.out.println("Introduceti numele profesorului: ");
        String nume = in.nextLine();
        Profesor p = findProfesorByName(nume);
        if(p == null){
            System.out.println("Introduceti id-ul profesorului: ");
            Long id = in.nextLong();
            p = findProfesorById(id);
            if(p == null){
                System.out.println("Nu exista niciun profesor cu id!");
                return;
            }
        }
        int ziuaCurenta = LocalDate.now().getDayOfWeek().getValue()-1;
        List<Ora> ore = profesorOreMap.get(p).stream().filter(o -> o.getZiuaSaptamanii() == ziuaCurenta).sorted().toList();
        for (Ora o : ore) {
            System.out.println(o.toStringProfesor());
        }
    }
}

