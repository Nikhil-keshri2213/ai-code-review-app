## Kafka Consumer Lag

- code-fetch-group: peak ~3696
- code-analysis-group: 0
- ai-review-group: 0
- storage-group: 0
- notification-service-group: 0

## Bottleneck Analysis

Initial issue:
- Messages concentrated in a single Kafka partition.

Fixes applied:
- Increased Kafka listener concurrency from 1 → 3
- Added PR-based Kafka message key for better partition distribution

Remaining observation:
- code-fetch-service still showed lag under 50 VU load due to GitHub API / file processing overhead.