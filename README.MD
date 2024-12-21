# Redis jEmbedded

Redis jEmbedded is a Java library that embeds Redis into your tests, enabling you to run Redis in a lightweight, standalone environment without the need for external dependencies. This simplifies the testing process and ensures consistent results across different environments.

## Table of Contents
1. [Installation](#installation)
2. [Usage](#usage)
3. [Contributing](#contributing)
4. [License](#license)
5. [Contact](#contact)

## Installation
To include Redis jEmbedded in your project, add the following dependency to your `pom.xml` if you are using Maven.
Note that if you want to use redis binary version 6, you can add `redis-jembedded-v6`:

```xml
<dependency>
    <groupId>io.josslab</groupId>
    <artifactId>redis-jembedded-v7</artifactId>
    <version>${redis binary version}</version>
</dependency>
```

Or add the following to your `build.gradle` if you are using Gradle:

```groovy
implementation 'io.josslab:redis-jembedded-v7:$REDIS_BINARY_VERSION'
```

## Usage
Here is an example of how to use Redis jEmbedded in your tests:

```java
import io.josslab.redis.jembedded.RedisServer;

public class ExampleTest {
    @Test
    public void testRedis() {
        RedisServer redisServer = RedisServer.newRedisServer().build();
        redisServer.start();

        // Your test code that interacts with Redis

        redisServer.stop();
    }
}
```

## Contributing
We welcome contributions! If you are interested in contributing to Redis jEmbedded, please follow these guidelines:
1. Fork the repository.
2. Create a new branch with a descriptive name.
3. Make your changes and commit them with clear and concise messages.
4. Open a pull request and provide a detailed description of your changes.

## License
Redis jEmbedded is distributed under the MIT License. See the [LICENSE](LICENSE) file for more information.

## Contact
If you have any questions or feedback, please feel free to reach out:
- Email: tran4774@gmail.com
- GitHub: [tran4774](https://github.com/tran4774)
- Org: [jOOS Lab](https://github.com/josslab)
