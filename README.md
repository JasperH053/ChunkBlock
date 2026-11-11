# ChunkBlock

ChunkBlock is a chunk-based land-claim plugin for Minecraft. It takes the progression of skyblock plugins like SuperiorSkyblock and ASkyBlock and moves it onto real land: instead of a floating island in the void, every team starts with a single chunk and grows its territory by levelling up.

## How it works

When you create a team you get your own chunk in the chunk world. That chunk is yours: outsiders can't break, build or open your chests, and you decide what else is allowed. Spending money in the upgrade menu raises your level, which expands your area chunk by chunk, from a single 1×1 chunk up to 22×22 at level 20. Each level also hands out rewards and unlocks new perks.

Your area is shown with a world border that you can recolour (blue, green or red) or switch off entirely from the border menu.

## Protection

Protection is powered by WorldGuard. Every team area is a WorldGuard region, and the settings lets the team toggle more than sixty flags without ever touching a command: PvP, TNT and creeper explosions, mob spawning, drops and much more. Flags that concern visitors, such as breaking blocks or opening doors, only apply to non-members, so your own team is never locked out of its own land.

## Commands

```
/chunkblock create           Create your team and first chunk
/chunkblock tp               Teleport to your chunk home
/chunkblock invite <player>  Invite a player to your team
/chunkblock kick <player>    Kick a team member
/chunkblock upgrade          Open the upgrade menu
/chunkblock level            Check your chunk level
/chunkblock top              View the leaderboard
/chunkblock settings         Manage your chunk settings
......................
```

## Configuration

Levels are defined in `levels.yml`. Each level sets the size of the area in blocks (always a multiple of 16), the price, the item rewards and the perks it unlocks:

```yaml
levels:
  2:
    chunkSize: 64
    cost: 500 # Any economy plugin supported
    rewards:
      DIAMOND_BLOCK: 25
    unlocks:
      - "god"
```

The toggles in the settings menu come from `settings.yml`. Every key is the name of a WorldGuard flag, with a display name and an icon. Remove a flag from the file and it disappears from the menu.

## Requirements

ChunkBlock started on "Paper 1.20.5" but is now getting updated to any other newer version of Paper. ChunkBlock needs WorldEdit, WorldGuard and Vault with an economy plugin. Data is (optionally) stored in SQLite.

## LARGE TODO's
- [ ] Add a block or entity that acts like a guide through the whole ChunkBlock experience. It gives you access to multipliers, it hands out quests and much more.
- [ ] Add a special mechanic, just like the Skyblock cobblestone generator or Oneblock generator.
- [ ] Add server-side custom resource-pack support. (ItemsAdder, Oraxen..)
- [ ] Add Vue dashboard with support for statistics using Python and SQLite.
- [ ] Add proper documentation, eventually a wiki.
- [ ] Add a public developer API.
- [ ] Learn how to get closer to production.
- [ ] Do a play-through with my girlfriend :D.

## SMALLER TODO's :'(

- [ ] Hand out level rewards and actually apply unlocked perks (`fly`, `heal`, boosts).
- [ ] Earn XP from placing blocks, killing mobs and completing quests.
- [ ] Chunk quests and challenges.
- [ ] Faster crop growth, boosted spawn rates and (ore generation*) as upgrades.
- [ ] Team roles (Owner, Admin, Member) with promote/demote.
- [ ] Team chat.
- [ ] Transfer a chunk to another player or team.
- [ ] Reset a chunk to its original state.
- [ ] Remember the chosen border colour after toggling it off and on.
- [ ] MySQL support next to SQLite.
- [ ] Dynmap / BlueMap integration.
- [ ] Custom rewards for upgrades such as Command line.

* I will probably won't add ore generation as it will affect the playing experience too much. 

NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.
