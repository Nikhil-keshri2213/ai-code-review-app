import { useState } from 'react'
import { C, Panel, Inset, GoldLine, SectionLabel } from './Skeubase'

const SEVERITY_CFG = {
  HIGH:   { text: '#ff4444', bg: '#1c0808', border: '#3a0f0f', glow: 'rgba(255,50,50,0.5)'  },
  MEDIUM: { text: '#ffaa00', bg: '#1c1100', border: '#3a2800', glow: 'rgba(255,160,0,0.5)'  },
  LOW:    { text: '#44ff88', bg: '#081c0e', border: '#0f3a1c', glow: 'rgba(60,255,100,0.5)' },
}

function SeverityBadge({ severity }) {
  const cfg = SEVERITY_CFG[severity] ?? SEVERITY_CFG.LOW
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 5,
      padding: '3px 8px',
      background: cfg.bg,
      border: `1px solid ${cfg.border}`,
      borderRadius: 4,
      fontFamily: C.mono,
      fontSize: 9,
      fontWeight: 700,
      color: cfg.text,
      textShadow: `0 0 7px ${cfg.glow}`,
      letterSpacing: '0.08em',
      boxShadow: `inset 0 1px 3px rgba(0,0,0,0.7), 0 0 5px ${cfg.glow}`,
      whiteSpace: 'nowrap',
    }}>
      {/* LED dot */}
      <span style={{
        width: 5, height: 5, borderRadius: '50%',
        background: cfg.text,
        boxShadow: `0 0 5px ${cfg.text}, 0 0 9px ${cfg.glow}`,
        flexShrink: 0,
      }} />
      {severity}
    </span>
  )
}

const HEADER_COLS = ['FILE', 'SEVERITY', 'CATEGORY', 'COMMENT']
const GRID = '150px 96px 135px 1fr'

export default function ReviewsList({ reviews }) {
  const [hovered, setHovered] = useState(null)

  if (!reviews?.length)
    return (
      <Panel style={{ padding: '20px 24px' }}>
        <p style={{ fontFamily: C.mono, fontSize: 11, color: C.textMuted }}>[ NO REVIEWS FOUND ]</p>
      </Panel>
    )

  return (
    <Panel screws>
      {/* <GoldLine /> */}
      <div style={{ padding: '20px 24px' }}>
        <SectionLabel right={`${reviews.length} ITEMS`}>► LATEST REVIEWS — PR #1</SectionLabel>

        <Inset>
          {/* Table header */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: GRID,
            padding: '9px 16px',
            background: 'linear-gradient(180deg, #161616, #111)',
            borderBottom: '1px solid #1e1e1e',
          }}>
            {HEADER_COLS.map(h => (
              <span key={h} style={{
                fontFamily: C.mono, fontSize: 8,
                letterSpacing: '0.16em', color: C.textMuted,
              }}>
                {h}
              </span>
            ))}
          </div>

          {/* Rows */}
          {reviews.slice(0, 10).map((r, i) => {
            const isHovered = hovered === r.id
            const isEven    = i % 2 === 0
            return (
              <div
                key={r.id}
                onMouseEnter={() => setHovered(r.id)}
                onMouseLeave={() => setHovered(null)}
                style={{
                  display: 'grid',
                  gridTemplateColumns: GRID,
                  padding: '10px 16px',
                  alignItems: 'center',
                  borderBottom: i < reviews.length - 1 && i < 9 ? '1px solid #131313' : 'none',
                  background: isHovered
                    ? 'rgba(200,144,10,0.06)'
                    : isEven ? 'transparent' : 'rgba(255,255,255,0.015)',
                  transition: 'background 0.15s',
                }}
              >
                {/* File name */}
                <span style={{
                  fontFamily: C.mono, fontSize: 11,
                  color: '#7a9ab8',
                  overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                }}>
                  {r.fileName}
                </span>

                {/* Severity badge */}
                <SeverityBadge severity={r.severity} />

                {/* Category */}
                <span style={{
                  fontFamily: C.mono, fontSize: 10,
                  color: C.textSecondary, letterSpacing: '0.04em',
                  textAlign: 'center',
                }}>
                  {r.category}
                </span>

                {/* Comment */}
                <span style={{
                  fontSize: 12, color: '#7a7268',
                  overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                }}>
                  {r.comment}
                </span>
              </div>
            )
          })}
        </Inset>
      </div>
    </Panel>
  )
}