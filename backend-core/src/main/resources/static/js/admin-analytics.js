/**
 * ADMIN ANALYTICS CONTROLLER
 * Gerencia os gráficos do painel administrativo usando ApexCharts.
 */

document.addEventListener("DOMContentLoaded", function () {

    // 1. GRÁFICO DE CRESCIMENTO (LINHA DUPLA)
    if (document.getElementById('chart-growth')) {
        const labels = JSON.parse(document.getElementById('data-chart-labels').textContent);
        const dataUsers = JSON.parse(document.getElementById('data-chart-users').textContent);
        const dataSimulados = JSON.parse(document.getElementById('data-chart-simulados').textContent);

        var optionsGrowth = {
            series: [{
                name: 'Novos Usuários',
                type: 'column',
                data: dataUsers
            }, {
                name: 'Simulados Realizados',
                type: 'line',
                data: dataSimulados
            }],
            chart: {
                height: 350,
                type: 'line',
                toolbar: { show: false },
                fontFamily: 'JetBrains Mono, monospace'
            },
            stroke: {
                width: [0, 4],
                curve: 'smooth'
            },
            colors: ['#000000', '#fbbf24'], // Preto (Barra), Amarelo (Linha)
            dataLabels: {
                enabled: true,
                enabledOnSeries: [1],
                style: { colors: ['#000'] }
            },
            labels: labels,
            xaxis: { type: 'category' },
            yaxis: [{
                title: { text: 'Usuários' },
            }, {
                opposite: true,
                title: { text: 'Simulados' }
            }]
        };

        new ApexCharts(document.querySelector("#chart-growth"), optionsGrowth).render();
    }

    // 2. GRÁFICO DE MATÉRIAS (ARANHA / RADAR ou BARRA)
    // Vamos usar BARRA HORIZONTAL para facilitar leitura
    if (document.getElementById('chart-subjects')) {
        const keys = JSON.parse(document.getElementById('data-chart-subjects-keys').textContent);
        const values = JSON.parse(document.getElementById('data-chart-subjects-values').textContent);

        var optionsSubjects = {
            series: [{
                data: values
            }],
            chart: {
                type: 'bar',
                height: 350,
                toolbar: { show: false },
                fontFamily: 'JetBrains Mono, monospace'
            },
            plotOptions: {
                bar: {
                    borderRadius: 0,
                    horizontal: true,
                    barHeight: '70%',
                    colors: {
                        ranges: [{
                            from: 0, to: 50, color: '#dc2626' // Vermelho se média baixa
                        }, {
                            from: 51, to: 100, color: '#16a34a' // Verde se média boa
                        }]
                    }
                }
            },
            dataLabels: { enabled: true },
            xaxis: {
                categories: keys,
            },
            title: {
                text: 'MÉDIA DE DESEMPENHO POR DISCIPLINA',
                align: 'center',
                style: { fontWeight: 900 }
            }
        };

        new ApexCharts(document.querySelector("#chart-subjects"), optionsSubjects).render();
    }

    // 3. SEVER STATUS (GAUGE)
    if (document.getElementById('chart-server')) {
        var optionsServer = {
            series: [42], // Mockado: CPU Load
            chart: {
                height: 250,
                type: 'radialBar',
                fontFamily: 'JetBrains Mono, monospace'
            },
            plotOptions: {
                radialBar: {
                    startAngle: -135,
                    endAngle: 135,
                    hollow: {
                        margin: 15,
                        size: '60%',
                        image: undefined,
                        imageOffsetX: 0,
                        imageOffsetY: 0,
                        position: 'front',
                    },
                    dataLabels: {
                        show: true,
                        name: {
                            offsetY: -10,
                            show: true,
                            color: '#888',
                            fontSize: '13px'
                        },
                        value: {
                            formatter: function (val) {
                                return parseInt(val) + "%";
                            },
                            color: '#111',
                            fontSize: '30px',
                            show: true,
                        }
                    }
                }
            },
            fill: {
                type: 'gradient',
                gradient: {
                    shade: 'dark',
                    type: 'horizontal',
                    shadeIntensity: 0.5,
                    gradientToColors: ['#dc2626'],
                    inverseColors: true,
                    opacityFrom: 1,
                    opacityTo: 1,
                    stops: [0, 100]
                }
            },
            stroke: {
                lineCap: 'round'
            },
            labels: ['CPU LOAD'],
        };
        new ApexCharts(document.querySelector("#chart-server"), optionsServer).render();
    }

});
