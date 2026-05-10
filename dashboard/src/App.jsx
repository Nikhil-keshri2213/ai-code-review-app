import { useEffect, useRef, useState } from 'react'
import { fetchStats, fetchTrends, fetchReviews } from './api'
import StatsCards  from './components/StatsCards'
import TrendsChart from './components/TrendsChart'
import ReviewsList from './components/ReviewsList'
import { C, Panel, Inset, GoldLine, LED } from './components/Skeubase'

/* ── Server health polling ────────────────────────────────────────────────── */
const HEALTH_URL      = 'http://localhost:8085/health'
const POLL_INTERVAL   = 10_000   // check every 10 seconds
const FETCH_TIMEOUT   = 4_000    // give up after 4 seconds

// status: 'checking' | 'online' | 'offline'
function useServerStatus() {
  const [status, setStatus] = useState('checking')
  const timerRef = useRef(null)

  async function ping() {
    const controller = new AbortController()
    const timeout = setTimeout(() => controller.abort(), FETCH_TIMEOUT)
    try {
      const res = await fetch(HEALTH_URL, { signal: controller.signal })
      setStatus(res.ok ? 'online' : 'offline')
    } catch {
      setStatus('offline')
    } finally {
      clearTimeout(timeout)
    }
  }

  useEffect(() => {
    ping()
    timerRef.current = setInterval(ping, POLL_INTERVAL)
    return () => clearInterval(timerRef.current)
  }, [])

  return status
}

const STATUS_CFG = {
  checking: { led: '#888880', glow: 'rgba(180,170,150,0.35)', label: 'CHECKING...', color: '#4a4840' },
  online:   { led: '#44ff88', glow: 'rgba(60,255,100,0.55)',  label: 'ONLINE',      color: '#3a6040' },
  offline:  { led: '#ff4444', glow: 'rgba(255,50,50,0.6)',    label: 'OFFLINE',     color: '#7a2828' },
}

/* ── Inject font once at the module level ─────────────────────────────────── */
if (typeof document !== 'undefined' && !document.getElementById('sku-fonts')) {
  const link = document.createElement('link')
  link.id   = 'sku-fonts'
  link.rel  = 'stylesheet'
  link.href = 'https://fonts.googleapis.com/css2?family=Share+Tech+Mono&family=Rajdhani:wght@500;700&display=swap'
  document.head.appendChild(link)
}

/* ── Subtle scanline overlay ──────────────────────────────────────────────── */
const PAGE_STYLE = {
  minHeight: '100vh',
  background: '#141414',
  backgroundImage: `
    repeating-linear-gradient(
      0deg,
      transparent, transparent 3px,
      rgba(255,255,255,0.007) 3px, rgba(255,255,255,0.007) 4px
    )
  `,
  padding: '28px 24px',
  fontFamily: "'Rajdhani', system-ui, -apple-system, sans-serif",
}

export default function App() {
  const serverStatus = useServerStatus()
  const sc = STATUS_CFG[serverStatus]

  const [stats,   setStats]   = useState(null)
  const [trends,  setTrends]  = useState([])
  const [reviews, setReviews] = useState([])
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState(null)

  useEffect(() => {
    Promise.all([fetchStats(), fetchTrends(), fetchReviews()])
      .then(([s, t, r]) => { setStats(s); setTrends(t); setReviews(r) })
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div style={PAGE_STYLE}>
      <div style={{ maxWidth: 1100, margin: '0 auto' }}>

        {/* ── Header ───────────────────────────────────────────────────── */}
        <Panel style={{ marginBottom: 24, overflow: 'hidden' }} screws>
          {/* <GoldLine /> */}
          <div style={{ padding: '20px 32px', display:'flex', alignItems:'center', gap:14 }}>
            {/* Icon badge */}
            <Inset style={{
              width: 44, height: 44, borderRadius: 8, flexShrink: 0,
              display:'flex', alignItems:'center', justifyContent:'center', fontSize: 22, color: C.textPrimary,
            }}>
              ⚙
            </Inset>

            <div>
              <h1 style={{
                fontFamily: C.mono, fontSize: 15, letterSpacing: '0.16em',
                color: C.textPrimary, fontWeight: 700, margin: 0,
                textShadow: '0 1px 0 rgba(255,255,255,0.08)',
              }}>
                AI CODE REVIEW DASHBOARD
              </h1>
              <p style={{
                fontFamily: C.mono, fontSize: 11, letterSpacing: '0.08em',
                color: C.textSecondary, margin: '4px 0 0',
              }}>
                ► Nikhil-keshri2213 / web-servers
              </p>
            </div>

            <div style={{ marginLeft:'auto', display:'flex', alignItems:'center', gap:7 }}>
              {/* LED pulses when checking */}
              <div style={{
                animation: serverStatus === 'checking' ? 'skuPulse 1.2s ease-in-out infinite' : 'none',
              }}>
                <LED color={sc.led} glow={sc.glow} />
              </div>
              <span style={{ fontFamily: C.mono, fontSize:9, letterSpacing:'0.12em', color: sc.color, transition:'color 0.4s' }}>
                {sc.label}
              </span>
              <style>{`@keyframes skuPulse { 0%,100%{opacity:1} 50%{opacity:0.3} }`}</style>
            </div>
          </div>
        </Panel>

        {/* ── Loading ──────────────────────────────────────────────────── */}
        {loading && (
          <div style={{
            textAlign:'center', padding:'60px 0',
            fontFamily: C.mono, fontSize:12, letterSpacing:'0.14em', color: C.textMuted,
          }}>
            [ LOADING DASHBOARD... ]
          </div>
        )}

        {/* ── Error ────────────────────────────────────────────────────── */}
        {error && (
          <div style={{
            background: 'linear-gradient(160deg, #1e0f0f 0%, #160a0a 100%)',
            border: '1px solid #4a1a1a',
            borderRadius: 12,
            padding: '16px 20px',
            marginBottom: 22,
            color: '#ff7070',
            boxShadow: 'inset 0 3px 10px rgba(0,0,0,0.8)',
            fontFamily: C.mono, fontSize: 12, letterSpacing: '0.06em',
          }}>
            ⚠ ERROR: {error} — make sure review-storage-service is running on :8085
          </div>
        )}

        {/* ── Dashboard ────────────────────────────────────────────────── */}
        {!loading && !error && (
          <>
            <StatsCards  stats={stats}   />
            <TrendsChart trends={trends} />
            <ReviewsList reviews={reviews} />
          </>
        )}

      </div>
    </div>
  )
}