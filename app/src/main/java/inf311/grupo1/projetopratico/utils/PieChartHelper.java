package inf311.grupo1.projetopratico.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import inf311.grupo1.projetopratico.models.ChartData;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitária moderna para configurar gráficos de pizza com design harmonioso
 */
public class PieChartHelper {
    
    private static final String TAG = "PieChartHelper";
    
    /**
     * Configura um gráfico de pizza moderno com os dados fornecidos
     * @param pieChart O componente PieChart a ser configurado
     * @param chartData Os dados do gráfico
     * @param isDashboard Se true, aplica configurações específicas para o dashboard
     */
    public static void setupPieChart(PieChart pieChart, ChartData.PieChartData chartData, boolean isDashboard) {
        try {
            if (pieChart == null || chartData == null) {
                Log.w(TAG, "PieChart ou dados são null");
                return;
            }
        
            List<PieEntry> entries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            
            // Usar as novas cores harmônicas
            String[] modernColors = AppConstants.CORES_GRAFICOS;
            int colorIndex = 0;
            
            for (ChartData.ConsultorData consultor : chartData.getConsultores()) {
                entries.add(new PieEntry(consultor.getLeads(), consultor.getNome()));
                
                // Usar cores da nova paleta harmônica
                String corModerna = modernColors[colorIndex % modernColors.length];
                colors.add(Color.parseColor(corModerna));
                colorIndex++;
            }

            PieDataSet dataSet = new PieDataSet(entries, "Leads por Consultor");
            dataSet.setColors(colors);
            
            // Configurações visuais modernas
            dataSet.setValueTextSize(isDashboard ? 13f : 15f);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
            
            // Melhorar espaçamento entre fatias
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(8f);
            
            // Formatter para valores
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });

            PieData data = new PieData(dataSet);
            pieChart.setData(data);

            // Configurações modernas de aparência
            pieChart.getDescription().setEnabled(false);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleRadius(isDashboard ? 40f : 45f);
            pieChart.setTransparentCircleRadius(isDashboard ? 45f : 50f);
            pieChart.setTransparentCircleColor(Color.parseColor("#f8fafc"));
            
            // Configurações de interação
            pieChart.setDrawEntryLabels(false);
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(true);
            
            // Animação suave
            pieChart.animateY(1200);

            // Configurar legenda moderna
            Legend legend = pieChart.getLegend();
            legend.setEnabled(true);
            legend.setTextColor(Color.parseColor("#374151"));
            legend.setTextSize(isDashboard ? 11f : 12f);
            legend.setTypeface(Typeface.DEFAULT);
            
            if (isDashboard) {
                // No dashboard, legenda compacta na parte inferior
                legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                legend.setDrawInside(false);
                legend.setWordWrapEnabled(true);
                legend.setMaxSizePercent(0.8f);
            } else {
                // Nas métricas, legenda vertical na lateral direita
                legend.setOrientation(Legend.LegendOrientation.VERTICAL);
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                legend.setDrawInside(false);
                legend.setXEntrySpace(8f);
                legend.setYEntrySpace(6f);
            }
            
            pieChart.invalidate();
            
            Log.d(TAG, "Gráfico de pizza moderno configurado com " + entries.size() + " entradas (Dashboard: " + isDashboard + ")");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar gráfico de pizza moderno", e);
        }
    }
    
    /**
     * Configuração específica para gráfico de pizza do dashboard com tema compacto
     */
    public static void setupDashboardPieChart(PieChart pieChart, ChartData.PieChartData chartData) {
        setupPieChart(pieChart, chartData, true);
        
        // Configurações adicionais específicas do dashboard
        if (pieChart != null) {
            pieChart.setExtraOffsets(10, 15, 10, 15);
            pieChart.setMinAngleForSlices(15f); // Ângulo mínimo para fatias pequenas
        }
    }
    
    /**
     * Configuração específica para gráfico de pizza das métricas com tema expandido
     */
    public static void setupMetricsPieChart(PieChart pieChart, ChartData.PieChartData chartData) {
        // Configurar o gráfico base primeiro (como métricas, não dashboard)
        setupPieChart(pieChart, chartData, false);
        
        // Configurações adicionais específicas das métricas
        if (pieChart != null) {
            // Configurações de layout para métricas - otimizado para melhor visualização
            pieChart.setExtraOffsets(10, 20, 10, 25);
            pieChart.setMinAngleForSlices(8f); // Ângulo mínimo menor para fatias pequenas
            
            // Hole radius otimizado para métricas
            pieChart.setHoleRadius(42f);
            pieChart.setTransparentCircleRadius(47f);
            
            // Seguir o mesmo padrão do dashboard, mas mantendo as características das métricas
            Legend legend = pieChart.getLegend();
            if (legend != null) {
                // Legenda na parte inferior como no dashboard para melhor organização
                legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                legend.setDrawInside(false);
                legend.setWordWrapEnabled(true);
                legend.setMaxSizePercent(0.90f); // Mais espaço para a legenda
                legend.setTextSize(12f); // Tamanho maior para métricas
                legend.setXEntrySpace(12f); // Mais espaço entre entradas
                legend.setYEntrySpace(8f);  // Mais espaço vertical
            }
            
            // Não usar valores percentuais nas métricas - mostrar valores absolutos como no dashboard
            PieData data = pieChart.getData();
            if (data != null && data.getDataSet() != null) {
                PieDataSet dataSet = (PieDataSet) data.getDataSet();
                
                // Configurações adicionais para melhor visibilidade
                dataSet.setValueTextSize(15f); // Texto maior nas fatias
                dataSet.setValueTextColor(Color.WHITE);
                dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
                dataSet.setSliceSpace(4f); // Mais espaço entre fatias
                dataSet.setSelectionShift(10f); // Maior deslocamento na seleção
                
                dataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.valueOf((int) value); // Valores absolutos como no dashboard
                    }
                });
            }
            
            // Configurações finais para garantir boa visualização
            pieChart.setDrawEntryLabels(false); // Não mostrar labels nas fatias para evitar poluição
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(true);
            
            pieChart.invalidate();
        }
    }
} 