homeSpotify.factory('Exercise1', function($resource) {
    return $resource('exercise1', [],{
        'loadStyles': {url : 'exercise1/all_styles', method:'GET', isArray:true, headers:{'Accept': 'application/json'}},
        'loadPerformers': {url : 'exercise1/performers_by_style/:style', method:'GET', isArray:true, headers:{'Accept': 'application/json'}},
        'verifyResults': {url : 'exercise1/verify_results', method:'GET', isArray:false, headers:{'Accept': 'application/json'}}
    });
});

homeSpotify.factory('Exercise2', function($resource) {
    return $resource('exercise2', [],{
        'verifyResults': {url : 'exercise2/verify_results', method:'GET', isArray:false, headers:{'Accept': 'application/json'}},
        'loadDistribution': {url : 'exercise2/distribution_by_type_and_style', method:'GET', isArray:false, headers:{'Accept': 'application/json'}}
    });
});

homeSpotify.factory('Exercise3', function($resource) {
    return $resource('exercise3', [],{
        'verifyResults': {url : 'exercise3/verify_results', method:'GET', isArray:false, headers:{'Accept': 'application/json'}},
        'loadTop10': {url : 'exercise3/top10_style', method:'GET', isArray:false, headers:{'Accept': 'application/json'}}
    });
});

homeSpotify.factory('Exercise4', function($resource) {
    return $resource('exercise4', [],{
        'verifyResults': {url : 'exercise4/verify_results', method:'GET', isArray:false, headers:{'Accept': 'application/json'}},
        'loadDecades': {url : 'exercise4/load_decades', method:'GET', isArray:true, headers:{'Accept': 'application/json'}},
        'loadCountriesForDecade': {url : 'exercise4/countries_for_decade/:decade', method:'GET', isArray:false, headers:{'Accept': 'application/json'}}
    });
});

homeSpotify.factory('Exercise5', function($resource) {
    return $resource('exercise5', [],{
        'verifyResults': {url : 'exercise5/verify_results', method:'GET', isArray:false, headers:{'Accept': 'application/json'}},
        'loadCountriesDistribution': {url : 'exercise5/countries_distribution', method:'GET', isArray:false, headers:{'Accept': 'application/json'}}
    });
});