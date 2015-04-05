homeSpotify.controller('TabsCtrl', function ($scope) {
    $scope.exerciseCount = 5;
    $scope.view_tab =  'home';

    $scope.changeTab = function(tab) {
        $scope.view_tab = tab;
    }
});

homeSpotify.controller('ExerciseCtrl', function($scope){

    $scope.openInstruction = false;
    $scope.openResult = true;
    $scope.openSolution = false;
    $scope.openHints = false;
    $scope.correctResults = false;

    $scope.verifyResults = function(resource) {
        resource.verifyResults()
            .$promise
            .then(function(response){
                $scope.correctResults = response.result;
            });
    };


});

homeSpotify.controller('Exercise1Ctrl', function(Exercise1, $scope){

    $scope.nameFilter = null;
    $scope.styles = [];
    $scope.performers = [];
    $scope.filteredPerformers = [];
    $scope.paginatedPerformers = [];
    $scope.currentPage = 1;
    $scope.itemsPerPage = 10;

    $scope.loadStyles = function() {
        Exercise1.loadStyles()
            .$promise
            .then(function(result){
                $scope.styles = result.sort();
            });
    };

    $scope.selectedStyle = null;
    $scope.changeStyle = function(selectedStyle) {
        if(selectedStyle) {
            Exercise1.loadPerformers({style: selectedStyle})
                .$promise
                .then(function(result){
                    $scope.performers = result;
                    $scope.filterByName();
                });

        }
    };

    $scope.filterByName = function() {
        if($scope.nameFilter) {
            $scope.filteredPerformers = $scope.performers.filter(function(performer) {
               return performer.toLowerCase().indexOf($scope.nameFilter.toLowerCase())>-1;
            });
        } else {
            $scope.filteredPerformers = $scope.performers;
        }
        $scope.paginatedPerformers = $scope.filteredPerformers.slice(0,10);
        $scope.currentPage = 1;
    };

    $scope.numPage = function(){
        return Math.ceil($scope.filteredPerformers.length/$scope.itemsPerPage);
    }

    $scope.pageChanged = function(){
        var begin = (($scope.currentPage - 1) * $scope.itemsPerPage)
            , end = begin + $scope.itemsPerPage;
        $scope.paginatedPerformers = $scope.filteredPerformers.slice(begin, end);
    }


    $scope.reload = function() {
        $scope.verifyResults(Exercise1);
        $scope.loadStyles();
        $scope.styles = [];
        $scope.performers = [];
        $scope.paginatedPerformers = [];
        $scope.currentPage = 1;
    }

    $scope.reload();

});

homeSpotify.controller('Exercise2Ctrl', function(Exercise2, $scope) {

    $scope.groupPie = null;
    $scope.artistPie = null;

    var self = this;

    this.initCharts = function(){
        $scope.groupPie = c3.generate({
            bindto: '#distribution_by_group_chart',
            data: {
                columns: [],
                type: 'pie'
            }
        });

        $scope.artistPie = c3.generate({
            bindto: '#distribution_by_artist_chart',
            data: {
                columns: [],
                type: 'pie'
            }
        });
    };

    $scope.reload = function() {
        $scope.verifyResults(Exercise2);
        self.loadData();
    }

    this.loadData = function() {
        Exercise2.loadDistribution()
            .$promise
            .then(function(response){
                if(response.group) {
                    $scope.groupPie.load({columns: response.group});
                } else {
                    $scope.groupPie.unload();
                }

                if(response.artist) {
                    $scope.artistPie.load({columns: response.artist});
                } else {
                    $scope.artistPie.unload();
                }

            });
    };

    self.initCharts();
    $scope.reload();
});

homeSpotify.controller('Exercise3Ctrl', function(Exercise3, $scope) {

    $scope.top10GroupPie = null;
    $scope.top10ArtistPie = null;
    var self = this;

    this.initCharts = function(){
        $scope.top10GroupPie = c3.generate({
            bindto: '#top10_by_group_chart',
            data: {
                columns: [],
                type: 'pie'
            },
            pie: {
                label: {
                    threshold: 0.02
                }
            }
        });

        $scope.top10ArtistPie = c3.generate({
            bindto: '#top10_by_artist_chart',
            data: {
                columns: [],
                type: 'pie'
            },
            pie: {
                label: {
                    threshold: 0.03
                }
            }
        });
    };

    $scope.reload = function() {
        $scope.verifyResults(Exercise3);
        self.loadData();
    }

    this.loadData = function() {
        $scope.top10GroupPie.unload();
        $scope.top10ArtistPie.unload();

        Exercise3.loadTop10()
            .$promise
            .then(function(response){
                if(response.group) {
                    $scope.top10GroupPie.load({columns: response.group});
                } else {
                    $scope.top10GroupPie.unload();
                }

                if(response.artist) {
                    $scope.top10ArtistPie.load({columns: response.artist});
                } else {
                    $scope.top10ArtistPie.unload();
                }

            });
    };

    self.initCharts();
    $scope.reload();
});

homeSpotify.controller('Exercise4Ctrl', function(Exercise4, $scope) {
    $scope.decades = [];
    $scope.selectedDecade = null;
    $scope.barChart = null;
    $scope.data = [];
    $scope.filteredData = [];
    $scope.lowThreshold=0;

    var self = this;

    this.initCharts = function() {
        $scope.barChart = c3.generate({
            bindto: '#country_distribution_chart',
            data: {
                columns: [],
                type: 'bar'
            },
            axis: {
                x: {
                    show: false
                },
                y: {
                    label: {
                        text: 'Albums count per decade',
                        position: 'outer-middle'
                    }
                }
            },
            tooltip: {
                grouped: false,
                format: {
                    title: function () { return 'Albums count'; }
                }
            },
            bar: {
                width: {
                    ratio: 1
                }
            }
        });

    };

    this.loadDecades = function() {
        Exercise4.loadDecades()
            .$promise
            .then(function(response) {
                $scope.decades = response;
            });
    };

    this.loadCountryForDecade = function(selectedDecade) {
        Exercise4.loadCountriesForDecade({decade: selectedDecade})
            .$promise
            .then(function(response) {
                if(response.countries) {
                    $scope.data = response.countries;
                    self.updateChart();
                } else {
                    $scope.barChart.unload();
                }
            });
    };

    this.updateChart = function() {
        self.initCharts();
        $scope.filteredData = $scope.data.filter(function(pair){
            return pair[1]>=$scope.lowThreshold;
        });
        $scope.barChart.load({columns: $scope.filteredData});

    };

    $scope.changeDecade = function(selectedDecade) {
        $scope.selectedDecade = selectedDecade;
        self.loadCountryForDecade(selectedDecade);
    };

    $scope.reload = function() {
        $scope.verifyResults(Exercise4);
        self.loadDecades();
    };

    self.initCharts();
    $scope.reload();

    $scope.$watch('lowThreshold',function(){
        self.updateChart();
    });
});

homeSpotify.controller('Exercise5Ctrl', function(Exercise5, $scope) {
    $scope.decades = [];
    $scope.countries = [];
    $scope.barChart = null;

    var self = this;

    this.initCharts = function() {
        $scope.barChart = c3.generate({
            bindto: '#country_distribution_by_decade_chart',
            data: {
                columns: [],
                type: 'bar',
                order: 'desc'
            },
            axis: {
                x: {
                    label: {
                        text: "Decade",
                        position: 'outer-center'
                    },
                    type: "category"
                },
                y: {
                    label: {
                        text: 'Albums count per decade',
                        position: 'outer-middle'
                    }
                }
            },
            tooltip: {
                format: {
                    title: function () { return 'Albums count'; }
                }

            },
            bar: {
                width: {
                    ratio: 1
                }
            },
            color: {

                pattern: ['#1F77B4', '#FF7F0E','#7F7F7F','#D62728','#9467BD','#FFFF00','#E377C2','#2CA02C','#BCBD22','#FFC0CB']
            }
        });

    };

    this.loadCountriesDistribution = function() {
        $scope.barChart.unload();
        Exercise5.loadCountriesDistribution()
            .$promise
            .then(function(response) {
                if(response.countries && response.decades && response.distribution) {
                    $scope.countries = response.countries;
                    $scope.decades = response.decades;
                    $scope.barChart.load({columns: response.distribution, categories:response.decades});
                    $scope.barChart.groups([$scope.countries]);
                }
            });
    };


    $scope.reload = function() {
        $scope.verifyResults(Exercise5);
        self.loadCountriesDistribution();
    };

    self.initCharts();
    $scope.reload();
});