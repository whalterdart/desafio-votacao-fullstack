import { describe, expect, it } from 'vitest'
import { secondsLeft } from './App'

describe('secondsLeft', () => {
  it('never returns a negative countdown', () => {
    expect(secondsLeft('2026-09-18T11:59:00Z', Date.parse('2026-09-18T12:00:00Z'))).toBe(0)
  })

  it('rounds remaining milliseconds up to the next second', () => {
    expect(secondsLeft('2026-09-18T12:00:02.100Z', Date.parse('2026-09-18T12:00:00Z'))).toBe(3)
  })
})

