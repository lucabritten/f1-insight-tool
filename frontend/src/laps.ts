import { LapsWithContext, Lap, ApiError } from "./types.js";
import { showError, getElement, callBackend } from "./utils.js";

export function initLap(): void {
    const form = document.getElementById("laps-form");
    if(form) 
        document.addEventListener("submit", handleLapsSubmit);
}

async function handleLapsSubmit(event:SubmitEvent): Promise<void> {
    event.preventDefault();

    const locationInput = document.getElementById("laps-location") as HTMLInputElement;
    const location = locationInput.value;

    const sessionInput = document.getElementById("laps-session") as HTMLInputElement;
    const session = sessionInput.value;

    const yearInput = document.getElementById("laps-year") as HTMLInputElement;
    const year = yearInput.value;

    const driverNumberInput = document.getElementById("driver-number") as HTMLInputElement;
    const driverNumber = driverNumberInput.value;

    const url = new URL("http://localhost:8080/laps");
    url.searchParams.append("location", location);
    url.searchParams.append("session", session);
    url.searchParams.append("year", year);
    url.searchParams.append("driver_number", driverNumber);

    const data: LapsWithContext | null = await callBackend<LapsWithContext>(url, "laps-loading-spinner", "laps-result");
    if(!data) return;

    renderLaps(data);
}

function renderLaps(data: LapsWithContext): void {
    const title = getElement<HTMLElement>("laps-title");
    const tbody = getElement<HTMLElement>("laps-body");

    title.textContent =
        `${data.driverName} – ${data.meetingName} (${data.sessionName})`;

    tbody.innerHTML = ""; // reset table

    // determine fastest lap (ignore pit out laps)
    let fastestLap: Lap;

    data.laps.forEach(lap => {
        if (!lap.isPitOutLap) {
            if (!fastestLap || lap.lapDuration < fastestLap.lapDuration)
                fastestLap = lap;
        }
    });

    //determine slowest lap (ignore pit out laps)
    let slowestLap: Lap;

    data.laps.forEach(lap => {
        if(!lap.isPitOutLap) {
            if(!slowestLap || lap.lapDuration > slowestLap.lapDuration)
                slowestLap = lap;
        }
    })

    data.laps.forEach(lap => {
        const row = document.createElement("tr");
        
        if (lap.isPitOutLap)
            row.classList.add("pit-out");

        if (fastestLap && lap.lapNumber === fastestLap.lapNumber)
            row.classList.add("fastest-lap");

        if(slowestLap && lap.lapNumber === slowestLap.lapNumber)
            row.classList.add("slowest-lap");
        
        row.innerHTML = `
            <td>${lap.lapNumber}</td>
            <td>${lap.durationSector1.toFixed(3)}</td>
            <td>${lap.durationSector2.toFixed(3)}</td>
            <td>${lap.durationSector3.toFixed(3)}</td>
            <td>${lap.lapDuration.toFixed(3)}</td>
            <td>${lap.isPitOutLap ? "Yes" : "No"}</td>
        `;

        tbody.appendChild(row);
    });
}