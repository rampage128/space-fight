var mine = null;

function add(perk, origin) {}

function remove(perk, origin) {}

function use(perk, origin) {
    if (mine != null) {
        mine.remove();
    }
    mine = script.spawnObject("gravitymine", origin);
}

function reset(perk, origin) {}