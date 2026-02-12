document.addEventListener("DOMContentLoaded", () => {
    const driverform = document.getElementById("driver-form");
    if (driverform) {
        driverform.addEventListener("submit", handleDriverSubmit);
    }

    const lapsform = document.getElementById("laps-form");
    if (lapsform) {
        lapsform.addEventListener("submit", handleLapsSubmit);
    }

    const resultsform = document.getElementById("results-form");
    if(resultsform) {
        resultsform.addEventListener("submit", handleResultsSubmit);
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

    const resultBox = document.getElementById("driver-result");
    resultBox.classList.add("hidden");
    console.log(url);

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

    const location = document.getElementById("laps-location").value;
    const session = document.getElementById("laps-session").value;
    const year = document.getElementById("laps-year").value;
    const driver_number = document.getElementById("driver-number").value;

    const url = new URL("http://localhost:8080/laps");
    url.searchParams.append("location", location);
    url.searchParams.append("session", session);
    url.searchParams.append("year", year);
    url.searchParams.append("driver_number", driver_number);

    const resultBox = document.getElementById("laps-result");
    if (resultBox) {
        resultBox.classList.add("hidden");
    }

    try {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error("HTTP error " ,response.status);
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

async function handleResultsSubmit(event) {
    event.preventDefault();

    const location = document.getElementById("results-location").value;
    const session = document.getElementById("results-session").value;
    const year = document.getElementById("results-year").value;

    const url = new URL("http://localhost:8080/session-result");
    url.searchParams.append("location", location);
    url.searchParams.append("session", session);
    url.searchParams.append("year", year);

    const resultBox = document.getElementById("session-result-result");
    if (resultBox) {
        resultBox.classList.add("hidden");
    }

    try {
        const response = await fetch(url);

        if(!response.ok) {
            throw new Error("HTTP error ", response.status);
        }

        const data = await response.json();
        renderSessionResults(data);
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
    const title = document.getElementById("laps-title");
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

function renderSessionResults(data) {
    const headRow = document.getElementById("results-table-head");
    const tbody = document.getElementById("results-table-body");

    const isQualifying =
        data.sessionName.toLowerCase().includes("quali");

        headRow.innerHTML = "";
        tbody.innerHTML = "";

        if(isQualifying) {
            headRow.innerHTML = `
                <th>Pos</th>
                <th>No</th>
                <th>Number</th>
                <th>Q1</th>
                <th>Q2</th>
                <th>Q3</th>
                <th>Gap</th>
            `;
        } else {
            headRow.innerHTML = `
                <th>Pos</th>
                <th>No</th>
                <th>Name</th>
                <th>Gap</th>
            `;
        }

        data.results.forEach((r, index) => {
            const row = document.createElement("tr");

            const position = r.position > 0 ? r.position : index + 1;

            if (isQualifying) {
                row.innerHTML = `
                    <td>${position}</td>
                    <td>${r.driverNumber}</td>
                    <td>${r.driverName}</td>
                    <td>${r.duration?.[0] ?? "-"}</td>
                    <td>${r.duration?.[1] ?? "-"}</td>
                    <td>${r.duration?.[2] ?? "-"}</td>
                    <td>${formatGap(r)}</td>
                `;
            } else {
                row.innerHTML = `
                    <td>${position}</td>
                    <td>${r.driverNumber}</td>
                    <td>${r.driverName}</td>
                    <td>${formatGap(r)}</td>
                `;
            }

            tbody.appendChild(row);
        })
}

function formatGap(r) {
    if (r.dsq) return "DSQ";
    if (r.dns) return "DNS";
    if (r.dnf) return "DNF";

    return r.gapToLeader?.[0] ?? "-";
}
