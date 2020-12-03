var paths;
var getPaths = fetch(window.location.href.replace("index.html", "beta-paths.js"))
                .then((response) => {return response.json()})
                .then((data) => {paths = data;})

const betaMarkPlugin = function() {
    return {
        wrapComponents: {
            OperationSummaryPath: (Original, { React }) => props => {
                const { specPath } = props;
                const method = specPath._tail.array[2].toUpperCase();
                const path = specPath._tail.array[1];
                if(paths !== undefined && paths[method] !== undefined && paths[method].includes(path)) {
                    return React.createElement("div", null,
                        React.createElement("span", {style: {paddingLeft: '10px', display: 'flex'}},
                            React.createElement('span', {style:{
                                fontSize: '14px',
                                fontWeight: '700',
                                padding: '6px',
                                color: 'white',
                                backgroundColor: 'red',
                                borderRadius: '3px'
                            }}, 'BETA'),
                            React.createElement(Original, {...props})
                        ),
                    );
                }
                return React.createElement(Original, props);
            }
        }
    }
}

export default betaMarkPlugin;