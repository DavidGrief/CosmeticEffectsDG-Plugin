CosmeticEffectsDG — версия для Paper/Spigot 1.16.5 + Java 16

Что внутри:
- Maven-проект
- Spigot API 1.16.5
- Java release 16
- plugin.yml с api-version 1.16
- config.yml, cosmetics.yml, messages.yml, players.yml
- 20 эффектов: flame_trail, ender_aura, heart_aura, magic_spiral, snow_trail, dragon_breath
- GUI /cosmetics
- команды выдачи/удаления/перезагрузки
- сохранение игроков в players.yml
- один repeating task для частиц

ВАЖНО:
Собирать нужно на JDK 16 или выше. Если у тебя стоит JDK 8/11, Maven выдаст ошибку target/release 16.

Сборка:
1. Открой папку CosmeticEffectsDG_1_16_5 в IntelliJ IDEA.
2. Убедись, что Project SDK = Java 16 или выше.
3. В Maven запусти:

mvn clean package

Готовый jar будет здесь:
target/CosmeticEffectsDG-1.0.0-1.16.5.jar

Установка:
1. Останови сервер.
2. Закинь jar в папку plugins.
3. Запусти сервер Paper/Spigot 1.16.5.
4. Выдай права:
   cosmetics.use
   cosmetics.admin
   cosmetics.give
   cosmetics.remove
   cosmetics.reload

Команды:
/cosmetics — меню
/cosmetic — алиас
/cosmetics off — выключить эффект
/cosmetics active — активный эффект
/cosmetics give <ник> <эффект> — выдать эффект
/cosmetics remove <ник> <эффект> — забрать эффект
/cosmetics clear <ник> — очистить косметику
/cosmetics list — список эффектов
/cosmetics reload — перезагрузка
/cosmetics setactive <ник> <эффект> — поставить активный эффект
/cosmetics info <ник> — инфа игрока

Изменения для 1.16.5:
- Paper API 1.21.11 заменён на Spigot API 1.16.5
- Java 21 заменена на Java 16
- api-version 1.21 заменён на 1.16
- Particle.SMOKE заменён на SMOKE_NORMAL
- Particle.ENCHANT заменён на ENCHANTMENT_TABLE
- Particle.SNOWFLAKE заменён на SNOW_SHOVEL
- Enchantment.UNBREAKING заменён на DURABILITY


Список эффектов:
- flame_trail
- ender_aura
- heart_aura
- magic_spiral
- snow_trail
- dragon_breath
- shadow_smoke
- emerald_spark
- witch_curse
- totem_guardian
- soul_flame
- lava_sparks
- ocean_splash
- crit_stars
- end_rod_crown
- nautilus_ocean
- angry_rage
- cloud_walker
- firework_spark
- slime_bounce
