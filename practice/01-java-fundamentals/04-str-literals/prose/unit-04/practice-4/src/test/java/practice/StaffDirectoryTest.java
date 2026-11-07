package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class StaffDirectoryTest {

    private Locale before;

    @BeforeEach
    void rememberTheDefaultLocale() {
        before = Locale.getDefault();
    }

    @AfterEach
    void restoreTheDefaultLocale() {
        Locale.setDefault(before);
    }

    private static StaffDirectory hired(String... names) {
        StaffDirectory d = new StaffDirectory();
        for (String n : names) d.hire(n);
        return d;
    }

    @Test
    void listsCapitalisedNamesAlphabetically() {
        StaffDirectory d = hired("Mira", "Ahmed", "Lucas", "Greta");

        assertThat(d.printedList(Locale.UK)).containsExactly("Ahmed", "Greta", "Lucas", "Mira");
    }

    @Test
    void aNameWrittenInLowercaseIsFiledUnderItsLetter() {
        StaffDirectory d = hired("Mira", "bram", "Carla", "Anouk");

        assertThat(d.printedList(Locale.UK)).containsExactly("Anouk", "bram", "Carla", "Mira");
    }

    @Test
    void anAccentedNameIsFiledUnderItsLetter() {
        StaffDirectory d = hired("Fatima", "\u00C9lodie", "Daniel", "Gustav");

        assertThat(d.printedList(Locale.FRANCE)).containsExactly("Daniel", "\u00C9lodie", "Fatima", "Gustav");
    }

    @Test
    void peopleWhoseNamesDifferOnlyInCaseOrAccentAreAllListed() {
        StaffDirectory d = hired("Noel", "Yusuf", "No\u00EBl", "noel", "Yusuf");

        assertThat(d.printedList(Locale.UK))
                .hasSize(5)
                .containsExactlyInAnyOrder("Noel", "No\u00EBl", "noel", "Yusuf", "Yusuf")
                .endsWith("Yusuf", "Yusuf");
    }

    @Test
    void eachOfficeGetsItsOwnLanguagesOrderWhateverTheServerDefault() {
        Locale.setDefault(Locale.GERMANY);
        StaffDirectory d = hired("\u00D6stlund", "Nyberg", "Zackrisson", "Persson");

        assertThat(d.printedList(Locale.forLanguageTag("sv-SE")))
                .containsExactly("Nyberg", "Persson", "Zackrisson", "\u00D6stlund");

        Locale.setDefault(Locale.forLanguageTag("sv-SE"));
        assertThat(d.printedList(Locale.GERMANY))
                .containsExactly("Nyberg", "\u00D6stlund", "Persson", "Zackrisson");
    }
}
