import { initDriver } from "./driver.js";
import { initLap } from "./laps.js";
import { initResult } from "./results.js";
import { initWeather } from "./weather.js";


document.addEventListener("DOMContentLoaded", () => {
    initDriver();
    initLap();
    initResult();
    initWeather();
});






