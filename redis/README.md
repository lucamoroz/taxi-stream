## Notes
- To interact with redis container via redis command line:
`docker exec -it taxistream_redis_1 redis-cli`

- Redis events are enabled: to subscribe to all emitted notifications run `psubscribe '__key*__:*'`

More info: https://redis.io/topics/notifications 


