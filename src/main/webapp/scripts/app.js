'use strict';

var homeSpotify = angular.module('HomeSpotify', ['ngRoute','ngResource','ui.bootstrap','debounce','ui.slider'])
    .config(function ($routeProvider, $httpProvider) {
        $httpProvider.defaults.headers.common = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'};

        $routeProvider
            .when('/home', {
                templateUrl: 'views/home.html'
            })
            .when('/exercise/:exercise_number', {
                templateUrl: function(params){ return 'views/exercise'+params.exercise_number+'.html' },
                controller: 'ExerciseCtrl'
            })
            .otherwise({
                redirectTo: '/home'
            });

});