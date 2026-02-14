import { SessionResult, SessionResultWithContext, ApiError } from "./types.js";
import { showError, getElement, callBackend } from "./utils.js";

export function initResult(): void {
    const form = document.getElementById("results-form");
    if(form)
        document.addEventListener("submit", handleResultsSubmit);
}


async function handleResultsSubmit(event: SubmitEvent): Promise<void> {
    event.preventDefault();

    const locationInput = document.getElementById("results-location") as HTMLInputElement;
    const location = locationInput.value;

    const sessionInput = document.getElementById("results-session") as HTMLInputElement;
    const session = sessionInput.value;

    const yearInput = document.getElementById("results-year") as HTMLInputElement;
    const year = yearInput.value;

    const url = new URL("http://localhost:8080/session-result");
    url.searchParams.append("location", location);
    url.searchParams.append("session", session);
    url.searchParams.append("year", year);
  
    const data: SessionResultWithContext | null = await callBackend<SessionResultWithContext>(url, "results-loading-spinner", "session-result-result");
    if(!data) return;

    renderSessionResults(data);
}

function renderSessionResults(data: SessionResultWithContext): void {
    const headRow = getElement<HTMLElement>("results-table-head");
    const tbody = getElement<HTMLElement>("results-table-body");

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
                    <td>+${formatGap(r)}</td>
                `;
            } else {
                row.innerHTML = `
                    <td>${position}</td>
                    <td>${r.driverNumber}</td>
                    <td>${r.driverName}</td>
                    <td>+${formatGap(r)}</td>
                `;
            }

            tbody.appendChild(row);
        })
}

function formatGap(r: SessionResult): string {
    if (r.dsq) return "DSQ";
    if (r.dns) return "DNS";
    if (r.dnf) return "DNF";

    return r.gapToLeader?.[0] ?? "-";
}
