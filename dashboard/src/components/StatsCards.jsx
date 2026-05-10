import { C, Panel, Inset, GoldLine, LED } from './Skeubase'

const CARDS = [
  { key: 'totalPRs',       label: 'TOTAL PRS',       ledColor: '#4ab8ff', glow: 'rgba(80,180,255,0.5)',  accent: '#4488cc' },
  { key: 'totalIssues',    label: 'TOTAL ISSUES',     ledColor: '#b8b0a0', glow: 'rgba(180,170,150,0.4)', accent: '#888880' },
  { key: 'highCount',      label: 'HIGH SEVERITY',    ledColor: '#ff5555', glow: 'rgba(255,60,60,0.6)',   accent: '#cc3333' },
  { key: 'avgIssuesPerPR', label: 'AVG ISSUES / PR',  ledColor: '#cc88ff', glow: 'rgba(200,130,255,0.5)', accent: '#8844cc' },
]

function StatCard({ label, value, ledColor, glow, accent }) {
  const display = typeof value === 'number'
    ? Number.isInteger(value) ? value.toLocaleString() : value.toFixed(1)
    : value ?? '—'

  return (
    <Panel style={{ overflow: 'hidden' }}>
      {/* Per-card color accent stripe */}
      {/* <div style={{ height: 2, background: `linear-gradient(90deg, transparent, ${accent}, transparent)` }} /> */}

      <div style={{ padding: '14px 16px 18px' }}>
        {/* Label row */}
        <div style={{ display:'flex', alignItems:'center', gap:6, marginBottom:10 }}>
          <LED color={ledColor} glow={glow} />
          <span style={{ fontFamily: C.mono, fontSize:9, letterSpacing:'0.13em', color: C.textSecondary }}>
            {label}
          </span>
        </div>

        {/* LCD number display */}
        <Inset style={{ padding:'10px 14px', borderRadius:8 }}>
          <span style={{
            fontFamily: C.mono,
            fontSize: 34,
            fontWeight: 700,
            color: ledColor,
            textShadow: `0 0 14px ${glow}, 0 0 4px ${ledColor}`,
            lineHeight: 1,
            letterSpacing: '0.04em',
          }}>
            {display}
          </span>
        </Inset>
      </div>
    </Panel>
  )
}

export default function StatsCards({ stats }) {
  if (!stats) return null

  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
      gap: 14,
      marginBottom: 22,
    }}>
      {CARDS.map(c => (
        <StatCard key={c.key} label={c.label} value={stats[c.key]} {...c} />
      ))}
    </div>
  )
}