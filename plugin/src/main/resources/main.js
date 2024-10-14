var globalTimeout = null;

var selectedConfiguration = null;
const tree = new Tree();

const TAB_GRAPH = "tab-graph"
const TAB_VERSIONS = "tab-versions"

var selectedTab = TAB_GRAPH
var filter = ""
var latestVersions = [];

function onLoad() {
  isLoading(true)
  tree.init(document.getElementsByName("tree-container")[0]);
  fetch("configurations.json")
    .then(response => response.json())
    .then(json => {
      setConfigurations(json);
    });
    document.getElementsByName("dependency-filter")[0].addEventListener('change', function (event) {
      event.preventDefault();
      if (globalTimeout != null) clearTimeout(globalTimeout);
      filter = event.target.value;
      globalTimeout = setTimeout(function () { tree.filter(event.target.value) }, 300);
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
  setStarupFlags(configurations.startupFlags)
  setVersion(configurations.version)
  selectConfiguration(configurations.configurations[0].name);
}

function setVersion(version){
  var versionContainer = document.getElementsByName("version")[0];
  versionContainer.innerHTML = version;
}

function setStarupFlags(flags) {
  var latestVersionLink = document.getElementsByName("latest-versions")[0];
  if (flags.fetchVersions === true) {
    latestVersionLink.classList.remove("is-hidden");
    latestVersionLink.addEventListener("click", function() {
        setTab(TAB_VERSIONS);
    });
    fetchLatestVersions()
  } else {
    latestVersionLink.classList.add("is-hidden");
  }
}

function selectConfiguration(name) {
  setTab(TAB_GRAPH);
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
    document.getElementsByName("selected-configuration")[0].innerHTML = selectedConfiguration;
    fetchConfiguration(name);
  }
  console.log("select: " + name);
}

function fetchConfiguration(name) {
  isLoading(true);
  fetch(name + "/dependencies.json")
    .then(response => response.json())
    .then(json => {
      tree.clear();
      tree.draw(json);
      tree.filter(this.filter);
      isLoading(false);
    })
}

function fetchLatestVersions(){
  fetch("latest-versions.json")
  .then(response => response.json())
  .then(json => {
    latestVersions = json;
      var container = document.getElementsByName("versions-table")[0]
      Object.keys(json).forEach(key=> {
        var library = key + ":" + json[key]
        container.innerHTML += '<a class="panel-block">'+ library +'</a>';
      })
  })
}

function isLoading(bool) {
  var elem = document.getElementsByName("progress-bar")[0]
  if (bool) {
    elem.classList.remove("is-hidden")
  } else {
    elem.classList.add("is-hidden")
  }
}


function setTab(tab) {
  console.log("setTab", tab);
  if (selectedTab === tab) return;
  selectedTab = tab;
  var tabs = document.querySelectorAll('[type="tab"]');
  console.log("tabs", tabs);
  tabs.forEach(t=> {
    if (t.attributes["tab"].value!=tab) {
      t.classList.add("is-hidden")
    } else {
      t.classList.remove("is-hidden")
    }
  });
}