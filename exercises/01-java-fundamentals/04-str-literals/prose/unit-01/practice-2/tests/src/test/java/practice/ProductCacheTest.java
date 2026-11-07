package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCacheTest {

    @Test
    void plainTextIsCountedAndTheOldestEntryMakesRoom() {
        ProductCache cache = new ProductCache(40);
        cache.put("SKU-1", "Oat milk 1 l");      // 5 + 12
        cache.put("SKU-2", "Rye bread");         // 5 + 9
        assertThat(cache.usedBytes()).isEqualTo(31);

        cache.put("SKU-3", "Apple juice");       // 5 + 11: over budget, SKU-1 goes
        assertThat(cache.get("SKU-1")).isNull();
        assertThat(cache.get("SKU-3")).isEqualTo("Apple juice");
        assertThat(cache.usedBytes()).isEqualTo(30);
    }

    @Test
    void accentedLatinTextCostsOneBytePerCharacter() {
        ProductCache cache = new ProductCache(1_000);
        cache.put("DK-4410", "Ærøskøbing honey 250 g");

        assertThat(cache.usedBytes()).isEqualTo(7 + 22);
    }

    @Test
    void oneCharacterBeyondLatin1MakesTheWholeDescriptionTwoBytesPerCharacter() {
        ProductCache cache = new ProductCache(1_000);
        cache.put("NL-0093", "Gift card 25 €");

        assertThat(cache.usedBytes()).isEqualTo(7 + 2 * 14);
    }

    @Test
    void textOutsideLatin1CostsTwoBytesPerCharacterWhateverItsUtf8Size() {
        ProductCache cache = new ProductCache(1_000);
        cache.put("JP-7781", "東京タワー模型");

        assertThat(cache.usedBytes()).isEqualTo(7 + 2 * 7);
    }
}
