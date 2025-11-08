package inf311.grupo1.projetopratico.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import inf311.grupo1.projetopratico.models.ChartData;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitária moderna para configurar gráficos de barras com design harmonioso
 */
public class BarChartHelper {
    
    private static final String TAG = "BarChartHelper";
    
    /**
     * Configura um gráfico de barras moderno para métricas da equipe
     */
    public static void setupTeamBarChart(BarChart barChart, ChartData.BarChartData chartData) {
        try {
            if (barChart == null || chartData == null) {
                Log.w(TAG, "BarChart ou dados são null");
                return;
            }
            
            List<BarEntry> leadsEntries = new ArrayList<>();
            List<BarEntry> conversoesEntries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            
            int index = 0;
            for (ChartData.ConsultorData consultor : chartData.getConsultores()) {
                leadsEntries.add(new BarEntry(index, consultor.getLeads()));
                conversoesEntries.add(new BarEntry(index, consultor.getConversoes()));
                labels.add(consultor.getNome());
                index++;
            }
            
            // Dataset moderno para leads
            BarDataSet leadsDataSet = new BarDataSet(leadsEntries, "Leads");
            leadsDataSet.setColor(Color.parseColor(AppConstants.COR_LEADS));
            leadsDataSet.setValueTextColor(Color.parseColor("#374151"));
            leadsDataSet.setValueTextSize(12f);
            leadsDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
            
            // Dataset moderno para conversões
            BarDataSet conversoesDataSet = new BarDataSet(conversoesEntries, "Conversões");
            conversoesDataSet.setColor(Color.parseColor(AppConstants.COR_CONVERSOES));
            conversoesDataSet.setValueTextColor(Color.parseColor("#374151"));
            conversoesDataSet.setValueTextSize(12f);
            conversoesDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
            
            List<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(leadsDataSet);
            dataSets.add(conversoesDataSet);
            
            BarData data = new BarData(dataSets);
            data.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });
            
            // Configurações visuais modernas
            data.setBarWidth(0.45f);
            barChart.setData(data);
            
            // Configurações gerais do gráfico
            setupModernBarChartAppearance(barChart, labels);
            
            Log.d(TAG, "Gráfico de barras da equipe configurado com " + labels.size() + " consultores");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar gráfico de barras da equipe", e);
        }
    }
    
    /**
     * Configura um gráfico de barras moderno para perfil individual
     */
    public static void setupProfileBarChart(BarChart barChart, ChartData.ConsultorData userData) {
        try {
            if (barChart == null || userData == null) {
                Log.w(TAG, "BarChart ou dados do usuário são null");
                return;
            }
            
            List<BarEntry> leadsEntries = new ArrayList<>();
            List<BarEntry> conversoesEntries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            
            // Adicionar dados do usuário
            leadsEntries.add(new BarEntry(0, userData.getLeads()));
            conversoesEntries.add(new BarEntry(0, userData.getConversoes()));
            labels.add("Meus Dados");
            
            // Dataset moderno para leads
            BarDataSet leadsDataSet = new BarDataSet(leadsEntries, "Leads");
            leadsDataSet.setColor(Color.parseColor(AppConstants.COR_LEADS));
            leadsDataSet.setValueTextColor(Color.parseColor("#374151"));
            leadsDataSet.setValueTextSize(14f);
            leadsDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
            
            // Dataset moderno para conversões
            BarDataSet conversoesDataSet = new BarDataSet(conversoesEntries, "Conversões");
            conversoesDataSet.setColor(Color.parseColor(AppConstants.COR_CONVERSOES));
            conversoesDataSet.setValueTextColor(Color.parseColor("#374151"));
            conversoesDataSet.setValueTextSize(14f);
            conversoesDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
            
            List<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(leadsDataSet);
            dataSets.add(conversoesDataSet);
            
            BarData data = new BarData(dataSets);
            data.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf((int) value);
                }
            });
            
            // Configurações visuais modernas
            data.setBarWidth(0.6f);
            barChart.setData(data);
            
            // Configurações específicas para perfil
            setupModernBarChartAppearance(barChart, labels);
            barChart.setFitBars(true);
            
            Log.d(TAG, "Gráfico de barras do perfil configurado para: " + userData.getNome());
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar gráfico de barras do perfil", e);
        }
    }
    
    /**
     * Aplica configurações visuais modernas para gráficos de barra
     */
    private static void setupModernBarChartAppearance(BarChart barChart, List<String> labels) {
        // Configurações gerais modernas
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setExtraOffsets(15f, 15f, 15f, 20f);
        
        // Animação suave
        barChart.animateY(1000);
        
        // Configurar eixo X moderno
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.parseColor("#e5e7eb"));
        xAxis.setAxisLineWidth(1f);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#6b7280"));
        xAxis.setTextSize(11f);
        xAxis.setTypeface(Typeface.DEFAULT);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index < labels.size() ? labels.get(index) : "";
            }
        });
        
        // Configurar eixo Y esquerdo moderno
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#f3f4f6"));
        leftAxis.setGridLineWidth(1f);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.parseColor("#6b7280"));
        leftAxis.setTextSize(10f);
        leftAxis.setTypeface(Typeface.DEFAULT);
        leftAxis.setGranularity(1f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        
        // Desabilitar eixo Y direito
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);
        
        // Configurar legenda moderna
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextColor(Color.parseColor("#374151"));
        legend.setTextSize(11f);
        legend.setTypeface(Typeface.DEFAULT);
        legend.setXEntrySpace(10f);
        legend.setYEntrySpace(5f);
        
        barChart.invalidate();
    }
    
    /**
     * Cria dados de fallback para demonstração quando não há dados reais
     */
    public static ChartData.ConsultorData createFallbackUserData(String userName) {
        return new ChartData.ConsultorData(
            userName != null ? userName : "Você", 
            0, 
            0, 
            AppConstants.COR_LEADS
        );
    }
} 