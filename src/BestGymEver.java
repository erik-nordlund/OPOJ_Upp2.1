import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class BestGymEver {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String KUND_FIL = "src/kunder.txt";
    private static final String PT_FIL = "logs/pt_log.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Skapar en scanner för att läsa in användarens input.
        List<Kund> kunder = läsInKunder(KUND_FIL);

        if (kunder.isEmpty()) { // Kontrollerar om listan med kunder är tom.
            System.out.println("Inga kunder kunde läsas in. Kontrollera att filen finns och är korrekt formaterad.");
            return;
        }

        System.out.print("Ange namn eller personnummer: ");
        String input = scanner.nextLine().trim();
        Kund hittadKund = sökKund(kunder, input);

        if (hittadKund != null) {
            if (harAktivMedlemskap(hittadKund)) { // Kontrollerar om kunden har ett aktivt medlemskap.
                System.out.println(hittadKund.namn + " är en nuvarande medlem.");
                loggaTräning(hittadKund);
            } else {
                System.out.println(hittadKund.namn + " är en före detta kund.");
            }
        } else {
            System.out.println("Personen finns inte i systemet och är obehörig.");
        }
    }

    private static List<Kund> läsInKunder(String filNamn) { // Metod som läser in kunder från filen.
        List<Kund> kunder = new ArrayList<>(); // Skapar en lista för att lagra kunder.

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filNamn))) {
            String rad;
            while ((rad = reader.readLine()) != null) { // Läser varje rad från filen tills slutet nås.
                try {
                    String[] delar = rad.split(", "); // Delar upp raden i personnummer och namn baserat på kommatecken och mellanslag.
                    if (delar.length != 2) { // Kontrollerar att raden är korrekt formaterad.
                        System.out.println("Felaktigt format i filen för raden: " + rad);
                        continue;
                    }
                    String personnummer = delar[0].trim();
                    String namn = delar[1].trim();
                    String datumRad = reader.readLine();

                    if (datumRad == null) { // Kontrollerar att datum finns för kunden.
                        System.out.println("Datum saknas för: " + namn);
                        continue;
                    }

                    LocalDate senasteBetalning = LocalDate.parse(datumRad.trim(), DATE_FORMAT);
                    kunder.add(new Kund(personnummer, namn, senasteBetalning));
                } catch (DateTimeParseException e) {
                    System.out.println("Felaktigt datumformat för raden: " + rad);
                }
            }
        } catch (NoSuchFileException e) {
            System.out.println("Filen kunde inte hittas: " + filNamn);
        } catch (IOException e) {
            System.out.println("Fel vid läsning av fil: " + e.getMessage());
        }

        return kunder;
    }

    private static Kund sökKund(List<Kund> kunder, String input) {
        for (Kund kund : kunder) {
            if (kund.personnummer.equals(input) || kund.namn.equalsIgnoreCase(input.trim())) { // Jämför kundens personnummer eller namn med användarens input.
                return kund;
            }
        }
        return null;
    }

    private static boolean harAktivMedlemskap(Kund kund) {
        LocalDate idag = LocalDate.now(); // Hämtar dagens datum.
        long dagarSedanBetalning = ChronoUnit.DAYS.between(kund.senasteBetalning, idag); // Beräknar antal dagar sedan senaste betalning.
        return dagarSedanBetalning <= 365;
    }

    private static void loggaTräning(Kund kund) {
        kontrolleraOchSkapaMapp("logs"); // Kontrollerar och skapar "logs"-mappen om den inte finns.

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(PT_FIL), StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            String datum = LocalDate.now().format(DATE_FORMAT); // Hämtar dagens datum.
            String tid = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")); // Hämtar tiden.

            writer.write(kund.personnummer + ", " + kund.namn + ", " + datum + ", " + tid); // Skriv kundens information och tid till loggfilen.
            writer.newLine(); // Lägger till en ny rad i loggfilen.
        } catch (IOException e) {
            System.out.println("Fel vid skrivning till PT-fil: " + e.getMessage());
        }
    }

    private static void kontrolleraOchSkapaMapp(String mappVäg) { // Metod som kontrollerar och skapar mapp om den saknas.
        Path logsPath = Paths.get(mappVäg); // Skapar en Path för den angivna mappen.
        if (Files.notExists(logsPath)) {
            try {
                Files.createDirectory(logsPath);
            } catch (IOException e) {
                System.out.println("Fel vid skapandet av mapp: " + e.getMessage());
            }
        }
    }
}