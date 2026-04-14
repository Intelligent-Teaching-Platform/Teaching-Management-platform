export function getToken() {
    return localStorage.getItem('system-token')
}

export function setToken(token) {
    return localStorage.setItem('system-token', token)
}

export function removeToken() {
    return localStorage.removeItem('system-token')
}