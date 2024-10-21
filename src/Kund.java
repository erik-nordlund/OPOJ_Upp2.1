import java.time.LocalDate;

public class Kund {
    String personnummer;
    String namn;
    LocalDate senasteBetalning;

    // Konstruktor för att skapa en kund med personnummer, namn och senaste betalningsdatum.
    public Kund(String personnummer, String namn, LocalDate senasteBetalning) {
        this.personnummer = personnummer;
        this.namn = namn;
        this.senasteBetalning = senasteBetalning;
    }
}