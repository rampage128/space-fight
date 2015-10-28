var buoy = null;

function add(perk, origin) {}

function remove(perk, origin) {}

function use(perk, origin) {
    if (buoy != null) {
        buoy.remove();
    }
    buoy = script.spawnObject("ammobuoy", origin);
}

function reset(perk, origin) {}