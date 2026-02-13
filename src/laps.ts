import { LapsWithContext, Lap, ApiError } from "./types";
import { showError, getElement } from "./utils";

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

    const resultBox = document.getElementById("laps-result");
    if (resultBox) {
        resultBox.classList.add("hidden");
    }

    try {
        const response = await fetch(url);

        if (!response.ok) {
            const errorData: ApiError = await response.json();
            throw new Error(`(${errorData.error}) with message: ${errorData.message}`);
        }

        const data: LapsWithContext = await response.json();
        renderLaps(data);

        if (resultBox) {
            resultBox.classList.remove("hidden");
        }
    } catch (error) {
        if (error instanceof Error)
            showError(error.message);
        else
            showError("An unknown error occurred")
    }
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
            if (!fastestLap || lap.lapDuration < fastestLap.lapDuration) {
                fastestLap = lap;
            }
        }
    });

    data.laps.forEach(lap => {
        const row = document.createElement("tr");
        
        if (lap.isPitOutLap) {
            row.classList.add("pit-out");
        }

        if (fastestLap && lap.lapNumber === fastestLap.lapNumber) {
            row.classList.add("fastest-lap");
        }

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