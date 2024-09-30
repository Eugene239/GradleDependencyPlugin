var globalTimeout = null;

var selectedConfiguration = null;
const graph = new Graph();

function onLoad() {
  isLoading(true)
  fetch("configurations.json")
    .then(response => response.json())
    .then(json => {
      setConfigurations(json);
      //       var configuration = json["configurations"][0].name
      //       fetch(configuration + "/dependencies.json")
      //           .then(response => response.json())
      //               .then(json => {
      //                   data = json
      //                   filteredData = json
      //                   fillConflicts()
      //                    drawTree()
      //     })
      //   })
      //   .catch(error => console.log(error))

      // document.getElementsByName("filter")[0].addEventListener('change', function (event) {
      //   event.preventDefault();
      //   if (globalTimeout != null) clearTimeout(globalTimeout);
      //   globalTimeout = setTimeout(function () { filter(data, event.target.value) }, 300);
    });
}

function setConfigurations(configurations) {
  var container = document.getElementsByName("configuration-list")[0];
  configurations.configurations.forEach(config => {
    container.innerHTML += '<a class="navbar-item" type="configuration-selector" config="' + config.name + '">' + config.name + '</a>';
  });
  var selectors = document.querySelectorAll('[type="configuration-selector"]');
  selectors.forEach(selector => {
    selector.addEventListener("click", function () { selectConfiguration(selector.attributes["config"].value) });
  });
  setVersion(configurations.version)
  selectConfiguration(configurations.configurations[0].name);
}

function setVersion(version){
  var versionContainer = document.getElementsByName("version")[0];
  versionContainer.innerHTML = version;
}

function selectConfiguration(name) {
  var selectors = document.querySelectorAll('[type="configuration-selector"]');
  selectors.forEach(selector => {
    if (selector.attributes["config"].value == name) {
      selector.classList.add("is-active");
    } else {
      selector.classList.remove("is-active");
    }
  });
  if (selectedConfiguration !== name) {
    selectedConfiguration = name;
    fetchConfiguration(name);
  }
  console.log("select: " + name);
}

function fetchConfiguration(name) {
  isLoading(true);
  fetch(name + "/dependencies.json")
    .then(response => response.json())
    .then(json => {
      graph.data = json
      graph.filteredData = json
      graph.fillConflicts()
      graph.drawTree()
      isLoading(false);
    })
}


document.addEventListener('click', function (event) {
  var target = event.target;
  var attribute = target.getAttribute(CONFLICT_ATTR);
  if (!attribute) return;
  event.preventDefault();
  // todo fix changing 
  document.getElementsByName("filter")[0].value = attribute;
  filter(data, attribute);
}, false);

function isLoading(bool) {
  var elem = document.getElementsByName("progress-bar")[0]
  if (bool) {
    elem.classList.remove("is-hidden")
  } else {
    elem.classList.add("is-hidden")
  }
}