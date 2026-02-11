document.addEventListener("DOMContentLoaded", () => {
    const driverForm = document.getElementById("driverForm");
    if (driverForm) {
        driverForm.addEventListener("submit", handleDriverSubmit);
    }

    const lapsForm = document.getElementById("lapsForm");
    if (lapsForm) {
        lapsForm.addEventListener("submit", handleLapsSubmit);
    }
});

async function handleDriverSubmit(event) {
    event.preventDefault();

    const firstName = document.getElementById("firstName").value;
    const lastName = document.getElementById("lastName").value;
    const year = document.getElementById("driver-year").value;

    const url = new URL("http://localhost:8080/driver");
    url.searchParams.append("firstName", firstName);
    url.searchParams.append("lastName", lastName);
    url.searchParams.append("year", year);

    const resultBox = document.getElementById("result");
    resultBox.classList.add("hidden");

    try {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error("HTTP error " + response.status);
        }

        const data = await response.json();
        renderDriver(data, year);
        resultBox.classList.remove("hidden");
    } catch (error) {
        showError(error.message);
    }
}

async function handleLapsSubmit(event) {
    event.preventDefault();

    const location = document.getElementById("location").value;
    const session = document.getElementById("session").value;
    const year = document.getElementById("laps-year").value;
    const driver_number = document.getElementById("driver_number").value;

    const url = new URL("http://localhost:8080/laps");
    url.searchParams.append("location", location);
    url.searchParams.append("session", session);
    url.searchParams.append("year", year);
    url.searchParams.append("driver_number", driver_number);

    const resultBox = document.getElementById("result");
    if (resultBox) {
        resultBox.classList.add("hidden");
    }

    try {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error("HTTP error " + response.status);
        }

        const data = await response.json();
        renderLaps(data);

        if (resultBox) {
            resultBox.classList.remove("hidden");
        }
    } catch (error) {
        showError(error.message);
    }
}

function renderDriver(data, year) {
    document.getElementById("res-name").textContent =
        data.firstName + " " + data.lastName;

    document.getElementById("res-team").textContent =
        data.teamName ?? "–";

    document.getElementById("res-year").textContent = year;
}

function showError(message) {
    alert("Error: " + message);
}

function renderLaps(data) {
    const title = document.getElementById("res-title");
    const tbody = document.getElementById("laps-body");

    title.textContent =
        `${data.driverName} – ${data.meetingName} (${data.sessionName})`;

    tbody.innerHTML = ""; // reset table

    // determine fastest lap (ignore pit out laps)
    let fastestLap = null;

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
