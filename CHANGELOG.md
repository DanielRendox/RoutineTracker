## 2023-12-20

### Changed
- Rewrite logic of habit status computation
- Refactor routine to habit
- Use HabitStatus instead of HistoricalStatus and PlanningStatus
- Add additional statuses for clarity
- Rewrite logic of habit status computation so that both historical and future statuses can be computed on the go instead of being cached in the database. The only data that needs to be stored is the number of times completed.