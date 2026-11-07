package practice;

import org.junit.jupiter.api.Test;
import practice.Sorting.Address;
import practice.Sorting.Parcel;
import practice.Sorting.Recipient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SortingTest {

    private static Parcel parcel(String id, String name, String street, String postcode) {
        return new Parcel(id, new Recipient(name, new Address(street, postcode)));
    }

    @Test
    void aParcelWithAFullAddressGoesToItsPostcodeBin() {
        Parcel p = parcel("PX-301", "Jane Doe", "4 Mill Lane", "KT19 8QS");

        assertThat(Sorting.postcodeOf(p)).contains("KT19 8QS");
        assertThat(Sorting.binFor(p)).isEqualTo("KT");
        assertThat(Sorting.binFor(parcel("PX-302", "Sam Poe", "1 Quay St", "EH6 6QQ"))).isEqualTo("EH");
    }

    @Test
    void aParcelWhoseAddressIsBeingConfirmedGoesToHold() {
        Parcel waiting = new Parcel("PX-410", new Recipient("Ann Example", null));
        Parcel next = parcel("PX-411", "Lee Sample", "9 Dock Rd", "BS1 4DJ");

        assertThat(Sorting.postcodeOf(waiting)).isEmpty();
        assertThat(List.of(waiting, next).stream().map(Sorting::binFor).toList()).containsExactly("HOLD", "BS");
    }

    @Test
    void aParcelWithNoRecipientOnFileGoesToHold() {
        Parcel unknown = new Parcel("PX-512", null);

        assertThat(Sorting.postcodeOf(unknown)).isEmpty();
        assertThat(Sorting.binFor(unknown)).isEqualTo("HOLD");
    }

    @Test
    void aParcelWithNoPostcodeEnteredGoesToHold() {
        Parcel blank = parcel("PX-613", "Kim Placeholder", "22 Hill View", null);

        assertThat(Sorting.postcodeOf(blank)).isEmpty();
        assertThat(Sorting.binFor(blank)).isEqualTo("HOLD");
    }
}
