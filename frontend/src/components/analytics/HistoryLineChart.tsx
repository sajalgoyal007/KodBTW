import React from 'react';

export interface HistoryChartPoint {
  date: string;
  value: number | null;
}

export interface HistoryChartSeries {
  name: string;
  color: string;
  points: HistoryChartPoint[];
}

interface HistoryLineChartProps {
  title: string;
  description: string;
  series: HistoryChartSeries[];
  emptyMessage: string;
}

const WIDTH = 760;
const HEIGHT = 260;
const LEFT = 48;
const RIGHT = 18;
const TOP = 20;
const BOTTOM = 42;

export const HistoryLineChart: React.FC<HistoryLineChartProps> = ({ title, description, series, emptyMessage }) => {
  const allDates = series.flatMap((line) => line.points.map((point) => point.date)).sort();
  const allValues = series.flatMap((line) => line.points.map((point) => point.value).filter((value): value is number => value !== null));
  const uniqueDates = [...new Set(allDates)];

  const formatDate = (value: string) => new Date(`${value}T00:00:00Z`).toLocaleDateString(undefined, { month: 'short', day: 'numeric', timeZone: 'UTC' });
  const hasValues = allValues.length > 0;
  const rawMin = hasValues ? Math.min(...allValues) : 0;
  const rawMax = hasValues ? Math.max(...allValues) : 1;
  const min = rawMin === rawMax ? rawMin - 1 : rawMin;
  const max = rawMin === rawMax ? rawMax + 1 : rawMax;
  const plotWidth = WIDTH - LEFT - RIGHT;
  const plotHeight = HEIGHT - TOP - BOTTOM;
  const xFor = (date: string) => {
    if (uniqueDates.length <= 1) return LEFT + plotWidth / 2;
    const index = uniqueDates.indexOf(date);
    return LEFT + (index / (uniqueDates.length - 1)) * plotWidth;
  };
  const yFor = (value: number) => TOP + ((max - value) / (max - min)) * plotHeight;

  const makeSegments = (line: HistoryChartSeries) => {
    const segments: HistoryChartPoint[][] = [];
    let active: HistoryChartPoint[] = [];
    line.points.forEach((point) => {
      if (point.value === null) {
        if (active.length) segments.push(active);
        active = [];
      } else active.push(point);
    });
    if (active.length) segments.push(active);
    return segments;
  };

  return (
    <section className="card history-chart-card">
      <div>
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
      {!hasValues ? (
        <div className="history-chart-empty">{emptyMessage}</div>
      ) : (
        <>
          <div className="history-chart-canvas">
            <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} role="img" aria-label={title}>
              {[0, 1, 2, 3].map((tick) => {
                const y = TOP + (tick / 3) * plotHeight;
                const value = Math.round(max - (tick / 3) * (max - min));
                return (
                  <g key={tick}>
                    <line x1={LEFT} y1={y} x2={WIDTH - RIGHT} y2={y} stroke="var(--color-border-subtle)" strokeDasharray="3 5" />
                    <text x={LEFT - 9} y={y + 4} textAnchor="end" className="history-axis-label">{value.toLocaleString()}</text>
                  </g>
                );
              })}
              {series.map((line) => (
                <g key={line.name}>
                  {makeSegments(line).map((segment, index) => (
                    <path
                      key={`${line.name}-${index}`}
                      d={segment.map((point, pointIndex) => `${pointIndex ? 'L' : 'M'} ${xFor(point.date)} ${yFor(point.value!)}`).join(' ')}
                      fill="none"
                      stroke={line.color}
                      strokeWidth="2.5"
                      strokeLinejoin="round"
                      strokeLinecap="round"
                    />
                  ))}
                  {line.points.filter((point) => point.value !== null).map((point) => (
                    <circle key={`${line.name}-${point.date}`} cx={xFor(point.date)} cy={yFor(point.value!)} r="4" fill={line.color}>
                      <title>{`${line.name} · ${formatDate(point.date)} · ${point.value!.toLocaleString()}`}</title>
                    </circle>
                  ))}
                </g>
              ))}
              {uniqueDates.length > 0 && (
                <>
                  <text x={LEFT} y={HEIGHT - 12} textAnchor="start" className="history-axis-label">{formatDate(uniqueDates[0])}</text>
                  <text x={WIDTH - RIGHT} y={HEIGHT - 12} textAnchor="end" className="history-axis-label">{formatDate(uniqueDates[uniqueDates.length - 1])}</text>
                </>
              )}
            </svg>
          </div>
          <div className="history-chart-legend">
            {series.filter((line) => line.points.some((point) => point.value !== null)).map((line) => (
              <span key={line.name}><i style={{ backgroundColor: line.color }} />{line.name}</span>
            ))}
          </div>
        </>
      )}
    </section>
  );
};
