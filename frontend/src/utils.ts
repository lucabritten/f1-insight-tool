export function showError(message: string) {
    alert("Error: " + message);
}

export function getElement<T extends HTMLElement>(id: string): T {
    const elem = document.getElementById(id);
    if(!elem)
        throw new Error(`Element with id '${id}' not found.`);

    return elem as T;
}