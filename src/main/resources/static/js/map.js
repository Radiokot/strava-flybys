let map
let result
let ownActivityLine

function onMapReady() {
    map = new google.maps.Map(document.getElementById('map'))
    loadTaskResult()
}

function loadTaskResult() {
    return fetch('/api/tasks/' + taskId)
        .then(response => response.json())
        .then(response => {
            result = response.state
            onTaskResultLoaded()
        })
}

function onTaskResultLoaded() {
    createAndDisplayOwnActivityLine()
    displayFlybys()
    if (result.flybys.length > 0) {
        selectFlyby(0)
    }
}

function createAndDisplayOwnActivityLine() {
    ownActivityLine = new google.maps.Polyline({
        path: result.activity.stream,
        geodesic: true,
        strokeColor: "#000000",
        strokeOpacity: 0.7,
        strokeWeight: 4,
    })

    ownActivityLine.setMap(map)
    const initialBounds = new google.maps.LatLngBounds()
    ownActivityLine.getPath().forEach((it) => initialBounds.extend(it))
    map.fitBounds(initialBounds)
}

function displayFlybys() {
    const list = document.getElementById('flybysList')

    result.flybys.forEach((flyby, i) => {
        const listItem = document.createElement('li')

        listItem.classList.add('list-group-item')
        listItem.onclick = () => { selectFlyby(i) }

        listItem.innerHTML = `
            <div class="row">
                <div class="col-9">
                    <a href="https://www.strava.com/activities/${flyby.activity.id}" target="_blank" style="text-decoration: none;">
                        <img src="${flyby.activity.athlete.avatar_url}" width="40" />
                    </a>
                    &nbsp;
                    ${flyby.activity.name}
                </div>
                <div class="col-3 align-self-center" style="text-align: end;">
                    C: ${flyby.correlation}%
                </div>
            </div>`

        list.appendChild(listItem)
    })
}

let currentSelectedFlybyIndex

function selectFlyby(index) {
    if (currentSelectedFlybyIndex == index) {
        return
    }

    currentSelectedFlybyIndex = index

    const list = document.getElementById('flybysList')
    Array.from(list.children).forEach((listItem, i) => {
        if (i == index) {
            listItem.classList.add('active')
        } else {
            listItem.classList.remove('active')
        }
    })

    displayFlybyLineAndAreas(result.flybys[index])
}

let currentFlybyLine
let currentNearbyLines

function displayFlybyLineAndAreas(flyby) {
    if (currentFlybyLine) {
        currentFlybyLine.setMap(null)
    }
    if (currentNearbyLines) {
        currentNearbyLines.forEach((line) => line.setMap(null))
    }

    const arrowSymbol = {
      path: google.maps.SymbolPath.FORWARD_OPEN_ARROW,
      scale: 1,
      strokeWeight: 4
    }

    currentFlybyLine = new google.maps.Polyline({
        path: flyby.activity.stream,
        geodesic: true,
        strokeColor: "#4985E9",
        strokeOpacity: 0.8,
        strokeWeight: 4,
        icons: [{
            icon: arrowSymbol,
            offset: '0',
            repeat: '20px'
        }],
    })
    currentFlybyLine.setMap(map)

    const bounds = new google.maps.LatLngBounds()
    ownActivityLine.getPath().forEach((it) => bounds.extend(it))
    currentFlybyLine.getPath().forEach((it) => bounds.extend(it))
    map.fitBounds(bounds)

    // Draw nearby points as ranges to avoid vierd "teleports".
    currentNearbyLines = []
    const nearbySet = new Set(flyby.nearby_points)
    let prevNearbyPoint
    let currentNearbyRange = new Set()
    let i = 0
    for (const nearbyPoint of nearbySet) {
        let createNewRange = false
        if (!prevNearbyPoint || nearbyPoint - prevNearbyPoint < 3) {
            currentNearbyRange.add(nearbyPoint)
        } else {
            createNewRange = true
        }

        if (i == nearbySet.size - 1 || createNewRange) {
            const rangeLine = new google.maps.Polyline({
                path: flyby.activity.stream.filter((it, j) => currentNearbyRange.has(j)),
                geodesic: true,
                strokeColor: "#2196f3",
                strokeOpacity: 0.5,
                strokeWeight: 15,
            })
            rangeLine.setMap(map)
            currentNearbyLines.push(rangeLine)
        }

        if (createNewRange) {
            currentNearbyRange = new Set()
        }

        prevNearbyPoint = nearbyPoint
        i++
    }
}