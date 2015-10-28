function onInit() {
    script.addCondition(TicketCondition);
    tickets = script.getCondition(TicketCondition);
}

function onUpdate() {
    checkDestroyers();
    checkConditions();
}

function onDestroy() {

}

/**
 * MISSION SPECIFIC FUNCTIONS
 */
var tickets    = null;
var destroyer1 = false;
var destroyer2 = false;
var tension    = false;

function checkDestroyers() {
    if ( script.isObjectDestroyed("destroyer_1") && !destroyer1 ) {
        tickets.setFactionTicketDrop(1, 0.001);
        tickets.removeTickets(1, tickets.getTickets(1) - script.getActiveClientFlights(1));
        
        script.addFactionScore(2, 1000);
        destroyer1 = true;
    }
    if ( script.isObjectDestroyed("destroyer_2") && !destroyer2 ) {
        tickets.setFactionTicketDrop(2, 0.001);
        tickets.removeTickets(2, tickets.getTickets(2) - script.getActiveClientFlights(2));
        
        script.addFactionScore(1, 1000);
        destroyer2 = true;
    }
}

function checkConditions() {
    var ticketsA = tickets.getTickets(1);
    var ticketsB = tickets.getTickets(2);
    if ( ticketsA < 1 ) {
        script.addFactionScore(2, 1000);
        script.endMission();
    }
    if ( ticketsB < 1 ) {
        script.addFactionScore(2, 1000);
        script.endMission();
    }
    if ( (ticketsA < 20 || ticketsB < 20) && !tension ) {
        tension = true;
        script.playMusic("tension_2.ogg", 0.5, true);
    }
}