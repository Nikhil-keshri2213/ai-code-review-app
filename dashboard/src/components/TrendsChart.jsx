import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'
import { C, Panel, Inset, GoldLine, LED, SectionLabel } from './Skeubase'

// Custom dark tooltip
const DarkTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null
  return (
    <div style={{
      background: 'linear-gradient(160deg, #222 0%, #1a1a1a 100%)',
      border: '1px solid #333',
      borderRadius: 8,
      padding: '10px 14px',
      boxShadow: '0 4px 16px rgba(0,0,0,0.8)',
    }}>
      <p style={{ fontFamily: C.mono, fontSize: 11, color: C.textSecondary, marginBottom: 6, letterSpacing: '0.08em' }}>
        {label}
      </p>
      {payload.map(p => (
        <p key={p.name} style={{
          fontFamily: C.mono, fontSize: 12,
          color: p.fill, margin: '2px 0',
          textShadow: `0 0 6px ${p.fill}`,
        }}>
          {p.name}: {p.value}
        </p>
      ))}
    </div>
  )
}

const LEGEND_ITEMS = [
  { label: 'HIGH',   color: '#cc2222', glow: 'rgba(200,40,40,0.5)'  },
  { label: 'MEDIUM', color: '#cc8800', glow: 'rgba(200,130,0,0.5)'  },
  { label: 'LOW',    color: '#228822', glow: 'rgba(40,180,40,0.5)'  },
]

export default function TrendsChart({ trends }) {
  if (!trends || trends.length === 0)
    return (
      <Panel style={{ marginBottom: 22, padding: '20px 24px' }}>
        <p style={{ fontFamily: C.mono, fontSize: 11, color: C.textMuted }}>[ NO TREND DATA ]</p>
      </Panel>
    )

  return (
    <Panel style={{ marginBottom: 22, overflow: 'hidden' }} screws>
      {/* <GoldLine /> */}
      <div style={{ padding: '20px 24px 24px' }}>
        <SectionLabel>► ISSUE TRENDS — LAST 12 WEEKS</SectionLabel>

        {/* Chart screen */}
        <Inset style={{ padding: '16px 8px 8px' }}>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={trends} margin={{ top: 4, right: 16, left: -12, bottom: 0 }}>
              <XAxis
                dataKey="weekLabel"
                tick={{ fontSize: 10, fill: '#4a4640', fontFamily: "'Courier New', monospace" }}
                axisLine={{ stroke: '#1e1e1e' }}
                tickLine={false}
              />
              <YAxis
                allowDecimals={false}
                tick={{ fontSize: 10, fill: '#4a4640', fontFamily: "'Courier New', monospace" }}
                axisLine={false}
                tickLine={false}
              />
              <Tooltip content={<DarkTooltip />} cursor={{ fill: 'rgba(255,255,255,0.03)' }} />
              <Bar dataKey="highCount"   name="HIGH"   fill="#cc2222" stackId="a" />
              <Bar dataKey="mediumCount" name="MEDIUM" fill="#cc8800" stackId="a" />
              <Bar dataKey="lowCount"    name="LOW"    fill="#228822" stackId="a" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Inset>

        {/* Custom legend */}
        <div style={{ display:'flex', gap:20, marginTop:14, paddingLeft:8 }}>
          {LEGEND_ITEMS.map(({ label, color, glow }) => (
            <div key={label} style={{ display:'flex', alignItems:'center', gap:6 }}>
              <LED color={color} glow={glow} />
              <span style={{ fontFamily: C.mono, fontSize: 9, letterSpacing:'0.1em', color: C.textSecondary }}>
                {label}
              </span>
            </div>
          ))}
        </div>
      </div>
    </Panel>
  )
}