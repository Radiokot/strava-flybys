function onFormSubmit() {
    const activityUrlRegex = /.+?strava\.com\/activities\/(\d+?)($|\?.+)/
    const input = document.getElementById('activityUrl').value
    const activityId = input.match(activityUrlRegex)[1]

    const errorAlert = document.getElementById('errorAlert')
    const formFieldset = document.getElementById('formFieldset')
    const processingHint = document.getElementById('processingHint')

    errorAlert.style.display = 'none'
    processingHint.style.display = 'block'
    formFieldset.setAttribute('disabled', 'true')

    submitTask(activityId)
        .then(response => response.json())
        .then(response => {
            processingHint.style.display = 'none'
            formFieldset.removeAttribute('disabled')
            window.location.href = '/tasks/' + response.task_id
        })
        .catch(error => {
            formFieldset.removeAttribute('disabled')
            processingHint.style.display = 'none'
            errorAlert.style.display = 'block'
            errorAlert.innerHTML = 'An error occurred. Try again later. ' +
                ((error.message) ? '<br>' + error.message : '')
        })
}

function submitTask(activityId) {
    return fetch('/api/tasks/', {
        method: 'POST',
        headers: {
              'Content-Type': 'application/json'
        },
        body: JSON.stringify({ activity_id: activityId })
    })
}