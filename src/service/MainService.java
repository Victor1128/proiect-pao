package service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import model.Materie.*;
import model.Ora.*;
import model.Profesor.*;
import model.Sala.*;
import model.Serie.*;
import model.Student.*;

public class MainService {
    private  List<Student> studenti = new ArrayList<>();
    private  List<Profesor> profesori = new ArrayList<>();
    private  List<Sala> sali = new ArrayList<>();
    private  List<Materie> materii = new ArrayList<>();
    private  List<Serie> serii = new ArrayList<>();
    private List<Ora> ore = new ArrayList<>();
    private final Map<String, List<Ora>> grupaOreMap = new HashMap<>();
    private final Map<Profesor, List<Ora>> profesorOreMap = new HashMap<>();
    private final List<String> zileleSaptamanii = Arrays.asList("Luni", "Marti", "Miercuri", "Joi", "Vineri", "Sambata", "Duminica");
    private final StudentDatabase studentDatabase = new StudentDatabase();
    private final ProfesorDatabase profesorDatabase = new ProfesorDatabase();
    private final MaterieDatabase materieDatabase = new MaterieDatabase();
    private final SalaDatabase salaDatabase = new SalaDatabase();
    private final SerieDatabase serieDatabase = new SerieDatabase();
    private final OraDatabase oraDatabase = new OraDatabase();

    public MainService() throws SQLException {
        studenti = studentDatabase.getAll();
        profesori = profesorDatabase.getAll();
        materii = materieDatabase.getAll();
        sali = salaDatabase.getAll();
        serii = serieDatabase.getAll();
        ore = oraDatabase.getAll();
        for (var ora : ore){
            if(!(ora instanceof Curs curs)){
                addOraToMaps(ora.getProfesor(), ora.getGrupa(), ora);
            }else{
                for (String g : curs.getSerie().getGrupe()) {
                    if(!grupaOreMap.containsKey(g))
                        grupaOreMap.put(g, new ArrayList<>());
                    grupaOreMap.get(g).add(ora);
                }
                if(!profesorOreMap.containsKey(curs.getProfesor()))
                    profesorOreMap.put(curs.getProfesor(), new ArrayList<>());
                profesorOreMap.get(curs.getProfesor()).add(ora);
            }
        }
    }

    private void addOraToMaps(Profesor p, String grupa, Ora ora) {
        if(!grupaOreMap.containsKey(grupa))
            grupaOreMap.put(grupa, new ArrayList<>());
        grupaOreMap.get(grupa).add(ora);
        if(!profesorOreMap.containsKey(p))
            profesorOreMap.put(p, new ArrayList<>());
        profesorOreMap.get(p).add(ora);
    }
    public void createStudent(Scanner in) throws SQLException {
        Student student = new Student(in);
        studentDatabase.add(student);
        studenti = studentDatabase.getAll();
        System.out.println("Studentul a fost creat cu succes!");
    }

    public void createProfesor(Scanner in) throws SQLException {
        Profesor profesor = new Profesor(in, materii);
        profesorDatabase.add(profesor);
        profesori = profesorDatabase.getAll();
        System.out.println("Profesorul a fost creat cu succes!");
    }

    public void createSerie(Scanner in) throws SQLException {
        Serie serie = new Serie(in);
        serieDatabase.add(serie);
        serii = serieDatabase.getAll();
        System.out.println("Serie creata cu succes!");
    }

    public void createMaterie(Scanner in) throws SQLException {
        Materie materie = new Materie(in, "Simple");
        materieDatabase.add(materie);
        materii = materieDatabase.getAll();
        System.out.println("Materia a fost creata cu succes!");
    }

    public void createSala(Scanner in) throws SQLException {
        int optiune = -1;
        while(optiune < 0 || optiune > 2) {
            System.out.println("Tip (0 - amfiteatru, 1 - laborator, 2 - seminar):");
            optiune = Integer.parseInt(in.nextLine());
        }
        switch (optiune) {
            case 0 -> salaDatabase.addAmfiteatru(new Amfiteatru(in));
            case 1 -> salaDatabase.addLaborator(new SalaLaborator(in, materii));
            case 2 -> salaDatabase.addSalaSeminar(new SalaSeminar(in));
        }
        sali = salaDatabase.getAll();
        System.out.println("Sala a fost creata cu succes!");
    }

    public void createOra(Scanner in) throws SQLException {
        int tip = -1;
        while(tip < 0 || tip > 2) {
            System.out.println("Tip (0 - curs, 1 - laborator, 2 - seminar):");
            tip = Integer.parseInt(in.nextLine());
        }
        switch (tip){
            case 0 -> {
                Curs curs = new Curs(in, profesori, materii, sali, serii);
                oraDatabase.addCurs(curs);
                for (String g : curs.getSerie().getGrupe()) {
                    if(!grupaOreMap.containsKey(g))
                        grupaOreMap.put(g, new ArrayList<>());
                    grupaOreMap.get(g).add(curs);
                }
                if(!profesorOreMap.containsKey(curs.getProfesor()))
                    profesorOreMap.put(curs.getProfesor(), new ArrayList<>());
                profesorOreMap.get(curs.getProfesor()).add(curs);
            }
            case 1 -> {
                Laborator laborator = new Laborator(in, profesori, materii, sali);
                oraDatabase.addLaborator(laborator);
                addOraToMaps(laborator.getProfesor(), laborator.getGrupa(), laborator);
            }
            case 2 -> {
                Seminar seminar = new Seminar(in, profesori, materii, sali);
                oraDatabase.addSeminar(seminar);
                addOraToMaps(seminar.getProfesor(), seminar.getGrupa(), seminar);
            }
        }
        ore = oraDatabase.getAll();
        System.out.println("Ora a fost adaugata cu succes!");
    }

    public Student getStudent(Scanner in){
        System.out.println("Introduceti numele studentului: ");
        String nume = in.nextLine();
        Student student;
        try {
          student = studentDatabase.getByName(nume);
        }
        catch (SQLException e){
          e.printStackTrace();
          return null;
        }
        if (student != null)
            System.out.println(student);
        else{
            System.out.println("Introduceti id-ul studentului: ");
            Long id = in.nextLong();
            try {
              System.out.println(studentDatabase.getById(id));
            }catch (SQLException e){
              e.printStackTrace();
            }
        }
        return student;
    }

    public Profesor getProfesor(Scanner in){
        System.out.println("Introduceti numele profesorului: ");
        String nume = in.nextLine();
        Profesor profesor;
        try{
          profesor = profesorDatabase.getByName(nume);
        }catch (SQLException e){
          e.printStackTrace();
          return null;
        }
        if(profesor != null) {
          System.out.println(profesor);
          return profesor;
        }
        System.out.println("Nu exista niciun profesor cu acest nume!");
        System.out.println("Introducesti id-ul profesorului");
        long id = Long.parseLong(in.nextLine());
        try{
          profesor = profesorDatabase.getById(id);
        }catch (SQLException e){
          e.printStackTrace();
        }
        System.out.println(profesor != null? profesor: "Nu exista niciun profesor cu acest id!");
        return profesor;
    }

    public void getMaterie(Scanner in){
      System.out.println("Introduceti numele materiei: ");
      String nume = in.nextLine();
      Materie materie = null;
      try{
        materie = materieDatabase.getByName(nume);
      }catch (SQLException e){
        e.printStackTrace();
        return;
      }
      if(materie != null){
        System.out.println(materie);
        return;
      }
      System.out.println("Nu exista nicio materie cu acest nume!");
      System.out.println("Introduceti id-ul materiei");
      long id = Long.parseLong(in.nextLine());
      try{
        materie = materieDatabase.getById(id);
      }catch (SQLException e){
        e.printStackTrace();
      }
      System.out.println(materie != null? materie : "Nu exista nicio materie cu acest nume");
    }

    public void getSerie(Scanner in){
      System.out.println("Introduceti numele seriei:");
      String nume = in.nextLine();
      Serie serie;
      try{
        serie = serieDatabase.getByName(nume);
      }catch (SQLException e){
        e.printStackTrace();
        return;
      }
      if(serie != null){
        System.out.println(serie);
        return;
      }
      System.out.println("Nu exista nicio serie cu acest nume");
      System.out.println("Introduceti id-ul seriei");
      long id = Long.parseLong(in.nextLine());
      try{
        serie = serieDatabase.getById(id);
      }catch (SQLException e){
        e.printStackTrace();
      }
      System.out.println(serie != null? serie: "Nu exista nicio serie cu acest id!");
    }
    public void deleteStudent(Scanner in){
      System.out.println("Introduceti ID-ul studentului: ");
      Long id = in.nextLong();
      try{
          int n = studentDatabase.delete(id);
          studenti = studentDatabase.getAll();
          System.out.println(n!=0 ? "Studentul a fost sters cu succes!" : "Eroare la stergerea studentului");
      } catch (SQLException e) {
          System.out.println("Nu exista niciun student cu acest ID!");
      }
    }

    public void deleteProfesor(Scanner in){
      System.out.println("Introduceti ID-ul profesorului: ");
      Long id = in.nextLong();
      try{
          profesorDatabase.delete(id);
          profesori = profesorDatabase.getAll();
          System.out.println("Profesorul a fost sters cu succes!");
      } catch (SQLException e) {
          System.out.println("Nu exista niciun profesor cu acest ID!");
      }
    }

    public void deleteOra(Scanner in){
        System.out.println("Introduceti ID-ul orei: ");
        Long id = in.nextLong();
        try{
            oraDatabase.delete(id);
            ore = oraDatabase.getAll();
            System.out.println("Ora a fost stearsa cu succes!");
        } catch (SQLException e) {
            System.out.println("Nu exista nicio ora cu acest ID!");
        }
    }

    public void deleteMaterie(Scanner in){
        System.out.println("Introduceti ID-ul materiei: ");
        Long id = in.nextLong();
        try{
            materieDatabase.delete(id);
            materii = materieDatabase.getAll();
            System.out.println("Materia a fost stearsa cu succes!");
        } catch (SQLException e) {
            System.out.println("Nu exista nicio materie cu acest ID!");
        }
    }

    public void deleteSerie(Scanner in){
        System.out.println("Introduceti ID-ul seriei: ");
        Long id = in.nextLong();
        try{
            serieDatabase.delete(id);
            serii = serieDatabase.getAll();
            System.out.println("Seria a fost stearsa cu succes!");
        } catch (SQLException e) {
            System.out.println("Nu exista nicio serie cu acest ID!");
        }
    }

    public void updateStudent(Scanner in){
      System.out.println("Introduceti id-ul studentului: ");
      Long id = Long.parseLong(in.nextLine());
      try {
        System.out.println(studentDatabase.getById(id));
      }catch (SQLException e){
        System.out.println("Nu exista niciun student cu acest ID!");
        return;
      }
      try{
        Student newStudent = new Student(in);
        newStudent.setId(id);
        studentDatabase.update(newStudent);
        studenti = studentDatabase.getAll();
      }catch(SQLException e){
        e.printStackTrace();
      }
    }

    public void updateProfesor(Scanner in){
      System.out.println("Introduceti id-ul profesorului");
      long id = Long.parseLong(in.nextLine());
      try{
        System.out.println(profesorDatabase.getById(id));
      }catch (SQLException e){
        System.out.println("Nu exista niciun presor cu acest ID!");
        return;
      }
      try{
        Profesor newProfesor = new Profesor(in, materii);
        newProfesor.setId(id);
        profesorDatabase.update(newProfesor);
        profesori = profesorDatabase.getAll();
      }catch (SQLException e){
        e.printStackTrace();
      }
    }

    public void updateMaterie(Scanner in){
      System.out.println("Introduceti id-ul materiei");
      long id = Long.parseLong(in.nextLine());
      try{
        System.out.println(materieDatabase.getById(id));
      }catch (SQLException e){
        System.out.println("Nu exista nicio materie cu acest ID");
      }
      try{
        Materie newMaterie = new Materie(in, "Simple");
        newMaterie.setId(id);
        materieDatabase.update(newMaterie);
        materii = materieDatabase.getAll();
      }catch (SQLException e){
        e.printStackTrace();
      }
    }

    public void updateSerie(Scanner in){
      System.out.println("Introduceti id-ul seriei");
      long id = Long.parseLong(in.nextLine());
      try{
        System.out.println(serieDatabase.getById(id));
      }catch (SQLException e){
        System.out.println("Nu exista nicio serie cu acest ID");
      }
      try{
        Serie newSerie = new Serie(in);
        newSerie.setId(id);
        serieDatabase.update(newSerie);
        serii = serieDatabase.getAll();
      }catch (SQLException e){
        e.printStackTrace();
      }
    }

    public void afisareOrarStudent(Scanner in){
      Student s = getStudent(in);
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
        Profesor p = getProfesor(in);
        try {
            Collections.sort(profesorOreMap.get(p));
        }catch (NullPointerException e){
            System.out.println("Profesorul nu are ore!");
            return;
        }
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
        Student s = getStudent(in);
        String grupa = s.getGrupa();
        int ziuaCurenta = LocalDate.now().getDayOfWeek().getValue()-1;
        List<Ora> ore = grupaOreMap.get(grupa).stream().filter(o -> o.getZiuaSaptamanii() == ziuaCurenta).sorted().toList();
        for (Ora o : ore) {
            System.out.println(o);
        }
    }

    public void afisareOrarProfesorZiCurenta(Scanner in){
        Profesor p = getProfesor(in);
        int ziuaCurenta = LocalDate.now().getDayOfWeek().getValue()-1;
        List<Ora> ore = profesorOreMap.get(p).stream().filter(o -> o.getZiuaSaptamanii() == ziuaCurenta).sorted().toList();
        for (Ora o : ore) {
            System.out.println(o.toStringProfesor());
        }
    }



    public void test() throws SQLException{
        oraDatabase.delete(1L);

    }
}