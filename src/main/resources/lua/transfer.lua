local fromBalance = tonumber(redis.call('GET', KEYS[1]) or '0')
local amount = tonumber(ARGV[1])

if fromBalance < amount then
    return 0
end

redis.call('DECRBY', KEYS[1], amount)
redis.call('INCRBY', KEYS[2], amount)
return 1