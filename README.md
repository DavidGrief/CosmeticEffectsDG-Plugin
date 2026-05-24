# CosmeticEffectsDG

Полный исходный код Minecraft-плагина косметики для Paper/Spigot **1.16.5** на **Java 16**.

Плагин сделан как source-available проект для GitHub: код можно открыть, посмотреть, изучить структуру и собрать через Maven.

## Возможности

- GUI-меню `/cosmetics`
- 20 косметических эффектов частиц
- выдача и удаление эффектов игрокам
- активный эффект сохраняется в `players.yml`
- все эффекты настраиваются в `cosmetics.yml`
- сообщения настраиваются в `messages.yml`
- поддержка `&` цветов и hex `&#RRGGBB`
- один repeating task для частиц
- оптимизация по дистанции видимости

## Команды

### Игрок

```text
/cosmetics
/cosmetic
/cosmetics off
/cosmetics active
```

### Админ

```text
/cosmetics give <player> <effect>
/cosmetics remove <player> <effect>
/cosmetics clear <player>
/cosmetics list
/cosmetics reload
/cosmetics setactive <player> <effect>
/cosmetics info <player>
```

## Права

```text
cosmetics.use
cosmetics.admin
cosmetics.give
cosmetics.remove
cosmetics.reload
cosmetics.bypass
```

## Эффекты

```text
flame_trail
ender_aura
heart_aura
magic_spiral
snow_trail
dragon_breath
shadow_smoke
emerald_spark
witch_curse
totem_guardian
soul_flame
lava_sparks
ocean_splash
crit_stars
end_rod_crown
nautilus_ocean
angry_rage
cloud_walker
firework_spark
slime_bounce
```


Готовый `.jar` появится в:

```text
target/CosmeticEffectsDG-1.0.0-1.16.5.jar
```

## Установка

1. Собери проект через Maven.
2. Закинь `.jar` в папку `plugins` сервера.
3. Запусти сервер.
4. Настрой `config.yml`, `cosmetics.yml`, `messages.yml`.

## Пример выдачи эффекта

```text
/cosmetics give Steve flame_trail
/cosmetics give Steve dragon_breath
```

## Лицензия

Смотри файл `LICENSE`. Код открыт для просмотра, но права на использование, продажу, перезалив и публикацию изменённых копий ограничены.
