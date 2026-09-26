package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeTest {

    @Test
    void theFullConstructorKeepsEveryValue() {
        Employee ann = new Employee("Ann", 7, "Sales");
        assertThat(ann.getName()).isEqualTo("Ann");
        assertThat(ann.getId()).isEqualTo(7);
        assertThat(ann.getDepartment()).isEqualTo("Sales");
    }

    @Test
    void twoArgumentsDefaultTheDepartment() {
        Employee ann = new Employee("Ann", 7);
        assertThat(ann.getName()).isEqualTo("Ann");
        assertThat(ann.getId()).isEqualTo(7);
        assertThat(ann.getDepartment()).isEqualTo("General");
    }

    @Test
    void oneArgumentDefaultsTheIdAndDepartment() {
        Employee ann = new Employee("Ann");
        assertThat(ann.getName()).isEqualTo("Ann");
        assertThat(ann.getId()).isZero();
        assertThat(ann.getDepartment()).isEqualTo("General");
    }

    @Test
    void everyConstructorRefusesABlankName() {
        assertThatThrownBy(() -> new Employee("  ", 1, "Sales")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Employee(null, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Employee("   ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Employee(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
