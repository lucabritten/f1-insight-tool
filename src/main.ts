import { initDriver } from "./driver";
import { initLap } from "./laps";
import { initResult } from "./results";


document.addEventListener("DOMContentLoaded", () => {
    initDriver();
    initLap();
    initResult();
});






