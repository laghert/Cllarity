package com.cllarity.app.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.cllarity.app.MainActivity
import com.cllarity.app.R
import com.cllarity.app.model.Habit
import com.cllarity.app.model.LlumieState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Natywny widget pulpitu Androida (Cllarity Zen Companion Widget).
 * Działa bez zewnętrznych bibliotek, bezawaryjnie na każdym Androidzie (API 26..34+).
 * Pokazuje w czasie rzeczywistym:
 * - Postać Llumie i poziom (Lvl)
 * - Passę dyscypliny (X dni)
 * - Liczbę ukończonych nawyków na dziś
 * - Status Codziennej Deklaracji 24h
 * - Bezpośrednie wywołanie aplikacji po stuknięciu w dowolne miejsce widgetu.
 */
class CllarityZenWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, CllarityZenWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                    ComponentName(context, CllarityZenWidgetProvider::class.java)
                )
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_cllarity_zen)

            // Odczyt danych z SharedPreferences v2
            val prefs = context.getSharedPreferences("cllarity_prefs_v2", Context.MODE_PRIVATE)
            val gson = Gson()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 1. Llumie State (Lvl & Streak)
            val llumieJson = prefs.getString("key_llumie_v2", null)
            val llumie = if (!llumieJson.isNullOrEmpty()) {
                try {
                    gson.fromJson(llumieJson, LlumieState::class.java)
                } catch (_: Exception) {
                    LlumieState()
                }
            } else LlumieState()

            views.setTextViewText(R.id.widget_level_chip, "Lvl ${llumie.level}")
            val streakText = if (llumie.streakDays == 1) "1 dzień dyscypliny" else "${llumie.streakDays} dni dyscypliny"
            views.setTextViewText(R.id.widget_streak_text, streakText)

            // 2. Nawyki na dziś
            val habitsJson = prefs.getString("key_habits_v2", null)
            var activeCount = 0
            var completedCount = 0
            if (!habitsJson.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<Habit>>() {}.type
                    val list: List<Habit> = gson.fromJson(habitsJson, type)
                    val activeHabits = list.filter { it.isActive }
                    activeCount = activeHabits.size
                    completedCount = activeHabits.count { it.lastCompletedDate == todayStr || it.isCompletedToday }
                } catch (_: Exception) {}
            }
            views.setTextViewText(R.id.widget_habits_summary, "$completedCount z $activeCount zrobionych dzisiaj")

            // 3. Status Deklaracji 24h
            val lastPledgeDate = prefs.getString("key_last_pledge_date_v2", null)
            val isPledged = lastPledgeDate == todayStr
            val pledgeText = if (isPledged) "Deklaracja 24h: Złożona ✓" else "Deklaracja: Czeka na Ciebie"
            views.setTextViewText(R.id.widget_pledge_status, pledgeText)

            // 4. Kliknięcie w widget otwiera aplikację
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
