# CardCatcherAPP

CardCatcher je natívna Android aplikácia (Kotlin + Jetpack Compose + Room) na lokálne vedenie skladu sealed Pokémon TCG produktov.

## Čo aplikácia vie

- lokálne uložiť nákupy a predaje (produkt, počet kusov, dátum, cena)
- evidovať skladové množstvo pre každý produkt
- ukázať štatistiky za:
  - dnes
  - týždeň
  - mesiac
  - 3 mesiace
  - rok
  - celý čas
- obsahuje základný katalóg produktov s orientačnými market cenami (EUR), ktoré vieš ďalej rozšíriť

## Technický základ

- Android app module `app`
- Lokálna databáza: Room (`cardcatcher.db`)
- UI: Jetpack Compose
- Štatistiky: `StatsCalculator`

## Spustenie testov

```bash
./gradlew :app:testDebugUnitTest
```
