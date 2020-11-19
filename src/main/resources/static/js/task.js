function displayCurrentState() {
    const taskStateSpan = document.getElementById('taskState')

    let stateDisplayValue = currentState + ' (unknown 🤔)'

    switch (currentState) {
        case 'scheduled':
            stateDisplayValue= 'waiting for execution'
            break
        case 'in_progress':
            stateDisplayValue= 'in progress'
            break
        case 'failed':
            stateDisplayValue= 'failed :('
            break
        case 'done':
            stateDisplayValue= 'done'
            break
    }

    taskStateSpan.innerHTML = stateDisplayValue
}

function onCurrentStateUpdated() {
    displayCurrentState()
    if (currentState == 'done') {
        window.location.href = `/tasks/${taskId}/map`
    }
}

function updateCurrentStateAndScheduleNext() {
    return fetch('/api/tasks/' + taskId)
        .then(response => response.json())
        .then(response => {
            currentState = response.state.name
            onCurrentStateUpdated()
            scheduleNextStateUpdate()
        })
        .catch(error => {
            scheduleNextStateUpdate(10000)
        })
}

function scheduleNextStateUpdate(timeout = 5000) {
    setTimeout(updateCurrentStateAndScheduleNext, timeout)
}