import { ApiError, LapsWithContext } from "./types";

export function showError(message: string) {
    alert("Error: " + message);
}

export function getElement<T extends HTMLElement>(id: string): T {
    const elem = document.getElementById(id);
    if(!elem)
        throw new Error(`Element with id '${id}' not found.`);

    return elem as T;
}

export async function callBackend<T>(
    url: URL, 
    spinnerId: string,
    resultBoxId: string
): Promise<T | null>{

    const spinner = getElement<HTMLElement>(spinnerId);
    spinner.classList.remove("hidden");

    const resultBox = getElement<HTMLElement>(resultBoxId);
    resultBox.classList.add("hidden");

    try {
        const response = await fetch(url);

        if(!response.ok) {
            const errorData: ApiError = await response.json();
            throw new Error(`(${errorData.error}) with message: ${errorData.message}`);
        }

        const data: T = await response.json();
        resultBox.classList.remove("hidden");
        return data;
    } catch (error) {
        if (error instanceof Error) 
            showError(error.message);
        else
            showError("An unknown error occurred.");
        return null;
    } finally {
            spinner.classList.add("hidden");
    }
}